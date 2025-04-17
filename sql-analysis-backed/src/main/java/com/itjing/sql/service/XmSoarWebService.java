package com.itjing.sql.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * 小米 soar web
 *
 * @author lijing
 * @date 2025-04-17
 */
@Service
public class XmSoarWebService {

	private static final Logger logger = LoggerFactory.getLogger(XmSoarWebService.class);

	/**
	 * SOAR Web 端口
	 */
	@Value("${soar.web.port:3000}")
	private int soarWebPort;

	/**
	 * 构建超时时间（秒）
	 */
	@Value("${soar.web.build.timeout:300}")
	private int buildTimeout;

	private final ResourceLoader resourceLoader;

	private Process soarWebProcess;

	private Path tempDir;

	private static final String TEMP_DIR_PREFIX = "soar-web-";

	private static final String EXECUTABLE_NAME = System.getProperty("os.name").toLowerCase().contains("windows")
			? "soar-web.exe" : "soar-web";

	public XmSoarWebService(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@PostConstruct
	public void initialize() {
		try {

			// 清理可能存在的旧临时目录
			cleanupOldTempDirs();

			// 创建临时目录
			createTempDirectory();

			// 添加关闭钩子
			// addShutdownHook();

			// 复制Go项目文件
			copyGoProject();

			// 构建Go项目
			buildGoProject();

			// 运行Go项目
			runGoProject();

			// 检查服务是否成功启动
			checkServiceHealth();
		}
		catch (Exception e) {
			logger.error("初始化Go项目失败", e);
			throw new RuntimeException("初始化Go项目失败", e);
		}
	}

	/**
	 * 清理旧临时目录
	 */
	private void cleanupOldTempDirs() {
		try {
			Path systemTempDir = Paths.get(System.getProperty("java.io.tmpdir"));
			Files.list(systemTempDir)
				.filter(path -> path.getFileName().toString().startsWith(TEMP_DIR_PREFIX))
				.forEach(path -> {
					try {
						deleteDirectory(path);
						logger.info("清理旧的临时目录: {}", path);
					}
					catch (IOException e) {
						logger.warn("清理旧的临时目录失败: {}", path, e);
					}
				});
		}
		catch (IOException e) {
			logger.warn("查找旧的临时目录失败", e);
		}
	}

	/**
	 * 删除目录
	 * @param directory 目录
	 * @throws IOException io异常
	 */
	private void deleteDirectory(Path directory) throws IOException {
		if (Files.exists(directory)) {
			Files.walk(directory)
				.sorted((a, b) -> -a.compareTo(b)) // 反向排序，确保先删除文件再删除目录
				.forEach(path -> {
					try {
						Files.deleteIfExists(path);
					}
					catch (IOException e) {
						logger.warn("删除文件失败: {}", path, e);
					}
				});
		}
	}

	/**
	 * 创建临时目录
	 * @throws IOException io异常
	 */
	private void createTempDirectory() throws IOException {
		// 使用固定前缀创建临时目录
		tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
		logger.info("创建临时目录: {}", tempDir);

		// 将临时目录路径写入文件，以便异常退出时也能找到
		Path tempDirFile = Paths.get(System.getProperty("java.io.tmpdir"), "soar-web-current.txt");
		Files.write(tempDirFile, tempDir.toString().getBytes());
	}

	/**
	 * 添加 Shutdown 钩子
	 */
	private void addShutdownHook() {
		// 添加JVM关闭钩子
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("执行关闭钩子，清理资源...");
			cleanup();
		}));
	}

	private void copyGoProject() throws IOException {
		logger.info("开始复制Go项目文件...");
		Resource resource = resourceLoader.getResource("classpath:soar-web");
		File sourceDir = resource.getFile();

		// 复制项目文件到临时目录
		Files.walk(sourceDir.toPath()).forEach(source -> {
			Path destination = tempDir.resolve(sourceDir.toPath().relativize(source));
			try {
				Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException e) {
				logger.error("复制文件失败: {}", source, e);
			}
		});

		logger.info("Go项目文件复制完成，目标目录: {}", tempDir);
	}

	/**
	 * 构建 Go 项目
	 * @throws IOException io异常
	 * @throws InterruptedException 中断异常
	 */
	private void buildGoProject() throws IOException, InterruptedException {
		logger.info("开始构建Go项目...");

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(tempDir.toFile());

		// 设置GOPROXY环境变量（国内加速）
		processBuilder.environment().put("GOPROXY", "https://goproxy.cn,direct");

		// 执行go mod tidy
		Process tidyProcess = processBuilder.command("go", "mod", "tidy").start();
		if (!tidyProcess.waitFor(300, TimeUnit.SECONDS)) {
			throw new RuntimeException("go mod tidy 执行超时");
		}

		// 构建输出路径
		Path outputPath = tempDir.resolve("bin").resolve(EXECUTABLE_NAME);
		// 确保输出目录存在
		Files.createDirectories(outputPath.getParent());

		logger.info("开始构建可执行文件，输出路径: {}", outputPath);

		// 执行go build，指定输出路径
		Process buildProcess = processBuilder.command("go", "build", "-o", outputPath.toString(), "main.go").start();

		// 读取构建过程的输出
		new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(buildProcess.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.info("build output: {}", line);
				}
			}
			catch (IOException e) {
				logger.error("读取构建输出失败", e);
			}
		}).start();

		// 读取构建过程的错误输出
		new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(buildProcess.getErrorStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.error("build error: {}", line);
				}
			}
			catch (IOException e) {
				logger.error("读取构建错误输出失败", e);
			}
		}).start();

		if (!buildProcess.waitFor(300, TimeUnit.SECONDS)) {
			throw new RuntimeException("go build 执行超时");
		}

		// 检查构建结果
		if (buildProcess.exitValue() != 0) {
			throw new RuntimeException("Go项目构建失败");
		}

		// 检查可执行文件是否存在
		if (!Files.exists(outputPath)) {
			throw new RuntimeException("构建完成但找不到可执行文件: " + outputPath);
		}

		// 设置执行权限（非Windows系统）
		if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
			outputPath.toFile().setExecutable(true);
		}

		logger.info("Go项目构建完成，可执行文件位置: {}", outputPath);
	}

	/**
	 * 运行 Go Project
	 * @throws IOException io异常
	 */

	private void runGoProject() throws IOException {
		logger.info("启动Go项目...");

		Path executablePath = tempDir.resolve("bin").resolve(EXECUTABLE_NAME);
		if (!Files.exists(executablePath)) {
			throw new RuntimeException("找不到可执行文件: " + executablePath);
		}

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(tempDir.toFile());

		// 修改启动命令，使用正确的参数格式
		processBuilder.command(executablePath.toString(), "--addr", ":" + soarWebPort // soar-web需要带冒号的端口格式
		);

		processBuilder.redirectErrorStream(true);

		logger.info("执行命令: {}", String.join(" ", processBuilder.command()));

		// 启动进程
		soarWebProcess = processBuilder.start();

		// 异步读取输出
		new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(soarWebProcess.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.info("soar-web: {}", line);
				}
			}
			catch (IOException e) {
				logger.error("读取soar-web输出失败", e);
			}
		}).start();

		// 等待确认进程启动
		try {
			Thread.sleep(2000);
			if (!soarWebProcess.isAlive()) {
				throw new RuntimeException("soar-web进程启动失败");
			}
			logger.info("soar-web服务已启动，端口: {}", soarWebPort);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("等待服务启动被中断", e);
		}
	}

	/**
	 * 检查服务运行状况
	 * @throws InterruptedException 中断异常
	 */
	private void checkServiceHealth() throws InterruptedException {
		logger.info("检查服务健康状态...");

		// 等待服务启动
		Thread.sleep(5000);

		// 检查进程是否存活
		if (!soarWebProcess.isAlive()) {
			throw new RuntimeException("soar-web进程已终止");
		}

		// TODO: 可以添加HTTP健康检查
		logger.info("服务健康检查通过");
	}

	@PreDestroy
	public void cleanup() {
		logger.info("开始清理资源...");

		// 1. 停止Go进程
		if (soarWebProcess != null) {
			try {
				// 尝试正常终止进程
				soarWebProcess.destroy();
				if (!soarWebProcess.waitFor(5, TimeUnit.SECONDS)) {
					// 如果正常终止失败，强制终止
					soarWebProcess.destroyForcibly();
					soarWebProcess.waitFor(5, TimeUnit.SECONDS);
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.warn("等待进程终止被中断", e);
			}
			soarWebProcess = null;
		}

		// 2. 清理临时目录
		if (tempDir != null) {
			try {
				deleteDirectory(tempDir);
				logger.info("临时目录已清理: {}", tempDir);

				// 删除临时目录记录文件
				Path tempDirFile = Paths.get(System.getProperty("java.io.tmpdir"), "soar-web-current.txt");
				Files.deleteIfExists(tempDirFile);
			}
			catch (IOException e) {
				logger.error("清理临时目录失败: {}", tempDir, e);
			}
			tempDir = null;
		}

		logger.info("资源清理完成");
	}

}
