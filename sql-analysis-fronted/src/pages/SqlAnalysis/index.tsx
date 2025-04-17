import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Button, message, Space, Typography, List, Modal, Popconfirm, Drawer, Table, Tooltip, Tag, Popover } from 'antd';
import { DatabaseOutlined, CodeOutlined, UserOutlined, LockOutlined, PlusOutlined, EditOutlined, DeleteOutlined, CopyOutlined, CheckCircleOutlined, WarningOutlined, InfoCircleOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import styles from './index.less';
import { testConnectionUsingPost, analyzeSqlUsingPost } from '@/services/backend/JdSqlAnalysisController';

const { TextArea } = Input;
const { Title, Text } = Typography;

// 添加文本截断工具函数
const truncateText = (text: string, maxLength: number = 15) => {
  if (!text) return '';
  return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text;
};

interface DataSource {
  id: string;
  name: string;
  url: string;
  username: string;
  password: string;
}

interface RuleResult {
  reason: string;
  suggestion: string;
  score: number;
}

interface AnalysisResult {
  sqlId: string | null;
  score: number;
  rules: RuleResult[];
}

interface ExplainResult {
  id: number;
  selectType: string;
  table: string;
  partitions: string | null;
  type: string;
  possibleKeys: string | null;
  key: string | null;
  keyLen: string | null;
  ref: string | null;
  rows: string;
  filtered: number;
  extra: string | null;
}

interface SqlAnalysisResponse {
  explainResultList: ExplainResult[];
  scoreResult: string;
}

const SqlAnalysis: React.FC = () => {
  const [form] = Form.useForm();
  const [dataSourceForm] = Form.useForm();
  const [testLoading, setTestLoading] = useState(false);
  const [analyzeLoading, setAnalyzeLoading] = useState(false);
  const [analysisResult, setAnalysisResult] = useState<string | null>(null);
  const [explainResult, setExplainResult] = useState<ExplainResult[]>([]);
  const [dbInfo, setDbInfo] = useState<any>(null);
  const [dataSources, setDataSources] = useState<DataSource[]>([]);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingDataSource, setEditingDataSource] = useState<DataSource | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [sqlValue, setSqlValue] = useState<string>('');

  // 从本地存储加载数据
  useEffect(() => {
    // 加载数据源列表
    const savedDataSources = localStorage.getItem('dataSources');
    if (savedDataSources) {
      setDataSources(JSON.parse(savedDataSources));
    }

    // 加载上次选中的数据源信息
    const lastDataSource = localStorage.getItem('lastDataSource');
    if (lastDataSource) {
      const parsedDataSource = JSON.parse(lastDataSource);
      form.setFieldsValue({
        url: parsedDataSource.url,
        username: parsedDataSource.username,
        password: parsedDataSource.password
      });
    }

    // 加载上次输入的SQL
    const lastSql = localStorage.getItem('lastSql');
    if (lastSql) {
      form.setFieldsValue({ sql: lastSql });
      setSqlValue(lastSql);
    }
  }, [form]);

  // 保存数据源到本地存储
  const saveDataSources = (sources: DataSource[]) => {
    localStorage.setItem('dataSources', JSON.stringify(sources));
    setDataSources(sources);
  };

  // 保存当前选中的数据源信息
  const saveCurrentDataSource = (values: { url: string; username: string; password: string }) => {
    localStorage.setItem('lastDataSource', JSON.stringify(values));
  };

  // 保存SQL语句
  const saveSqlValue = (sql: string) => {
    localStorage.setItem('lastSql', sql);
    setSqlValue(sql);
  };

  const handleTestConnection = async () => {
    setTestLoading(true);
    try {
      const values = await form.validateFields(['url', 'username', 'password']);
      saveCurrentDataSource(values);
      const res = await testConnectionUsingPost(values);
      if (res.code === 200) {
        message.success('连接成功');
        setDbInfo(res.data);
      }
    } catch (error) {
      message.error('连接失败');
    } finally {
      setTestLoading(false);
    }
  };

  const handleAnalyzeSql = async (values: API.SqlAnalyzeRequest) => {
    setAnalyzeLoading(true);
    try {
      saveSqlValue(values.sql);
      const res = await analyzeSqlUsingPost(values);
      if (res.code === 200) {
        message.success('分析成功');
        setAnalysisResult(res.data.scoreResult);
        setExplainResult(res.data.explainResultList);
      }
    } catch (error) {
      message.error('分析失败');
    } finally {
      setAnalyzeLoading(false);
    }
  };

  const showAddDataSourceModal = () => {
    setEditingDataSource(null);
    dataSourceForm.resetFields();
    setModalVisible(true);
  };

  const showEditDataSourceModal = (dataSource: DataSource) => {
    setEditingDataSource(dataSource);
    dataSourceForm.setFieldsValue(dataSource);
    setModalVisible(true);
  };

  const handleDeleteDataSource = (id: string) => {
    const newDataSources = dataSources.filter(ds => ds.id !== id);
    saveDataSources(newDataSources);
    message.success('删除成功');
  };

  const handleDataSourceFormSubmit = () => {
    dataSourceForm.validateFields().then(values => {
      if (editingDataSource) {
        // 编辑模式
        const newDataSources = dataSources.map(ds =>
          ds.id === editingDataSource.id ? { ...values, id: editingDataSource.id } : ds
        );
        saveDataSources(newDataSources);
        message.success('修改成功');
      } else {
        // 添加模式
        const newDataSource = {
          ...values,
          id: Date.now().toString(),
        };
        saveDataSources([...dataSources, newDataSource]);
        message.success('添加成功');
      }
      setModalVisible(false);
    });
  };

  const selectDataSource = (dataSource: DataSource) => {
    const values = {
      url: dataSource.url,
      username: dataSource.username,
      password: dataSource.password
    };
    form.setFieldsValue(values);
    saveCurrentDataSource(values);
    setDrawerVisible(false);
  };

  // 复制SQL语句
  const copySqlToClipboard = () => {
    if (!sqlValue) {
      message.warning('SQL语句为空');
      return;
    }

    navigator.clipboard.writeText(sqlValue)
      .then(() => {
        message.success('复制成功');
      })
      .catch(() => {
        message.error('复制失败');
      });
  };

  // 复制分析结果
  const copyAnalysisResult = () => {
    if (!analysisResult) {
      message.warning('分析结果为空');
      return;
    }

    navigator.clipboard.writeText(analysisResult.toString())
      .catch(() => {
        message.error('复制失败');
      });
  };

  const handleSqlChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newSql = e.target.value;
    setSqlValue(newSql);
    saveSqlValue(newSql);
  };

  const formatAnalysisResult = (result: string): AnalysisResult | null => {
    try {
      const lines = result.split('\n').filter(line => line.trim());
      let currentRule: Partial<RuleResult> = {};
      const rules: RuleResult[] = [];
      let sqlId: string | null = null;
      let totalScore: number = 0;

      lines.forEach(line => {
        if (line.includes('SQL 语句 ID：')) {
          sqlId = line.split('：')[1].trim();
        } else if (line.includes('SQL分析结果的分数为:')) {
          totalScore = parseInt(line.match(/\d+/)?.[0] || '0');
        } else if (line.includes('规则命中原因：')) {
          if (Object.keys(currentRule).length > 0) {
            rules.push(currentRule as RuleResult);
          }
          currentRule = {
            reason: line.split('："')[1].replace('"', '')
          };
        } else if (line.includes('规则命中，修改建议：')) {
          currentRule.suggestion = line.split('："')[1].replace('"', '');
        } else if (line.includes('规则命中，减去分数') || line.includes('规则命中，加上分数：')) {
          const scoreMatch = line.match(/[+-]\d+/);
          if (scoreMatch) {
            currentRule.score = parseInt(scoreMatch[0]);
          }
        }
      });

      if (Object.keys(currentRule).length > 0) {
        rules.push(currentRule as RuleResult);
      }

      return {
        sqlId,
        score: totalScore,
        rules
      };
    } catch (error) {
      console.error('解析分析结果失败:', error);
      return null;
    }
  };

  const renderAnalysisResult = (rawResult: string) => {
    const result = formatAnalysisResult(rawResult);
    if (!result) return <pre>{rawResult}</pre>;

    return (
      <div className={styles.analysisResult}>
        <div className={styles.scoreSection}>
          <Title level={4}>SQL分析得分</Title>
          <div className={styles.scoreDisplay}>
            <div className={`${styles.scoreCircle} ${result.score >= 80 ? styles.good : result.score >= 60 ? styles.warning : styles.bad}`}>
              {result.score}
            </div>
            <div className={styles.scoreText}>
              {result.score >= 80 ? (
                <Tag color="success" icon={<CheckCircleOutlined />}>性能良好</Tag>
              ) : result.score >= 60 ? (
                <Tag color="warning" icon={<WarningOutlined />}>需要优化</Tag>
              ) : (
                <Tag color="error" icon={<WarningOutlined />}>性能较差</Tag>
              )}
            </div>
          </div>
        </div>

        <div className={styles.rulesSection}>
          <Title level={4}>分析详情</Title>
          {result.rules.map((rule, index) => (
            <Card
              key={index}
              className={`${styles.ruleCard} ${rule.score > 0 ? styles.positiveRule : styles.negativeRule}`}
              size="small"
              bordered={false}
            >
              <Space direction="vertical" style={{ width: '100%' }}>
                <div className={styles.ruleHeader}>
                  <Text strong>规则 {index + 1}</Text>
                  <Tag color={rule.score > 0 ? 'success' : 'error'}>
                    {rule.score > 0 ? '+' : ''}{rule.score} 分
                  </Tag>
                </div>
                <div className={styles.ruleContent}>
                  <div>
                    <InfoCircleOutlined /> 命中原因：{rule.reason}
                  </div>
                  <div>
                    <WarningOutlined /> 修改建议：{rule.suggestion}
                  </div>
                </div>
              </Space>
            </Card>
          ))}
        </div>
      </div>
    );
  };

  // 执行计划说明内容
  const explainHelp = (
    <div style={{ maxWidth: 600, maxHeight: 400, overflow: 'auto' }}>
      <Typography.Paragraph>
        explain 主要用来 SQL 分析，它主要的属性详解如下：
      </Typography.Paragraph>
      <ul>
        <li><strong>id</strong>：查询的执行顺序的标识符，值越大优先级越高。简单查询的 id 通常为 1，复杂查询（如包含子查询或 UNION）的 id 会有多个。</li>
        <li><strong>select_type（重要）</strong>：查询的类型，如 SIMPLE（简单查询）、PRIMARY（主查询）、SUBQUERY（子查询）等。</li>
        <li><strong>table</strong>：查询的数据表。</li>
        <li><strong>type（重要）</strong>：访问类型，如 ALL（全表扫描）、index（索引扫描）、range（范围扫描）等。一般来说，性能从好到差的顺序是：const {'>'} eq_ref {'>'} ref {'>'} range {'>'} index {'>'} ALL。</li>
        <li><strong>possible_keys</strong>：可能用到的索引。</li>
        <li><strong>key（重要）</strong>：实际用到的索引。</li>
        <li><strong>key_len</strong>：用到索引的长度。</li>
        <li><strong>ref</strong>：显示索引的哪一列被使用。</li>
        <li><strong>rows（重要）</strong>：估计要扫描的行数，值越小越好。</li>
        <li><strong>filtered</strong>：显示查询条件过滤掉的行的百分比。一个高百分比表示查询条件的选择性好。</li>
        <li><strong>Extra（重要）</strong>：额外信息，如 Using index（表示使用覆盖索引）、Using where（表示使用 WHERE 条件进行过滤）、Using temporary（表示使用临时表）、Using filesort（表示需要额外的排序步骤）。</li>
      </ul>
      <Typography.Title level={5}>type 详解</Typography.Title>
      <ul>
        <li><strong>system</strong>：表示查询的表只有一行（系统表）。这是一个特殊的情况，不常见。</li>
        <li><strong>const</strong>：表示查询的表最多只有一行匹配结果。这通常发生在查询条件是主键或唯一索引，并且是常量比较。</li>
        <li><strong>eq_ref</strong>：表示对于每个来自前一张表的行，MySQL 仅访问一次这个表。这通常发生在连接查询中使用主键或唯一索引的情况下。</li>
        <li><strong>ref</strong>：MySQL 使用非唯一索引扫描来查找行。查询条件使用的索引是非唯一的（如普通索引）。</li>
        <li><strong>range</strong>：表示 MySQL 会扫描表的一部分，而不是全部行。范围扫描通常出现在使用索引的范围查询中（如 BETWEEN、{'">'}, {'<'}, {'>='}, {'<='})。</li>
        <li><strong>index</strong>：表示 MySQL 扫描索引中的所有行，而不是表中的所有行。即使索引列的值覆盖查询，也需要扫描整个索引。</li>
        <li><strong>all（性能最差）</strong>：表示 MySQL 需要扫描表中的所有行，即全表扫描。通常出现在没有索引的查询条件中。</li>
      </ul>
    </div>
  );

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '数据库URL',
      dataIndex: 'url',
      key: 'url',
      ellipsis: true,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: DataSource) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={(e) => {
            e.stopPropagation(); // 阻止事件冒泡
            showEditDataSourceModal(record);
          }}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该数据源吗？"
            onConfirm={() => {
              handleDeleteDataSource(record.id);
            }}
            okText="是"
            cancelText="否"
          >
            <Button type="link" danger icon={<DeleteOutlined />} onClick={(e) => e.stopPropagation()}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Typography.Title level={4}>SQL分析工具</Typography.Title>
          <Button
            type="primary"
            icon={<DatabaseOutlined />}
            onClick={() => setDrawerVisible(true)}
          >
            数据源管理
          </Button>
        </div>

        <Form
          form={form}
          layout="vertical"
          onFinish={handleAnalyzeSql}
        >
          <Form.Item
            label="数据库连接配置"
            required
          >
            <Space direction="vertical" style={{ width: '100%' }}>
              <Form.Item
                name="url"
                rules={[{ required: true, message: '请输入数据库url' }]}
              >
                <Input placeholder="数据库url" />
              </Form.Item>
              <Form.Item
                name="username"
                rules={[{ required: true, message: '请输入用户名' }]}
              >
                <Input placeholder="用户名" />
              </Form.Item>
              <Form.Item
                name="password"
                rules={[{ required: true, message: '请输入密码' }]}
              >
                <Input.Password placeholder="密码" />
              </Form.Item>
              <Button
                type="primary"
                onClick={handleTestConnection}
                loading={testLoading}
              >
                测试连接
              </Button>
            </Space>
          </Form.Item>

          {dbInfo && (
            <Card title="数据库信息" className={styles.resultCard}>
              <p>数据库产品名称: {dbInfo.databaseProductName}</p>
              <p>数据库版本: {dbInfo.databaseVersion}</p>
              <p>用户名: {dbInfo.userName}</p>
              <p>连接URL: {dbInfo.connectionUrl}</p>
              <p>支持事务: {dbInfo.transactionSupported ? '是' : '否'}</p>
            </Card>
          )}

          <Form.Item
            name="sql"
            label={
              <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                <span>SQL语句</span>
                <Tooltip title="复制SQL语句">
                  <Button
                    type="text"
                    icon={<CopyOutlined />}
                    onClick={copySqlToClipboard}
                    size="small"
                  />
                </Tooltip>
              </div>
            }
            rules={[{ required: true, message: '请输入SQL语句' }]}
          >
            <TextArea
              rows={6}
              placeholder="请输入需要分析的SQL语句"
              onChange={handleSqlChange}
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={analyzeLoading}>
              分析SQL
            </Button>
          </Form.Item>
        </Form>

        {explainResult && explainResult.length > 0 && (
          <Card
            title={
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>执行计划结果</span>
                <Popover
                  content={explainHelp}
                  title="执行计划说明"
                  trigger="click"
                  placement="leftTop"
                >
                  <Button type="text" icon={<QuestionCircleOutlined />}>
                    查看说明
                  </Button>
                </Popover>
              </div>
            }
            className={styles.resultCard}
          >
            <Table
              dataSource={explainResult}
              columns={[
                {
                  title: 'ID',
                  dataIndex: 'id',
                  key: 'id',
                  width: 60,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (id) => (
                    <Tooltip placement="topLeft" title={id}>
                      {id}
                    </Tooltip>
                  ),
                },
                {
                  title: '查询类型(select_type)',
                  dataIndex: 'selectType',
                  key: 'selectType',
                  width: 150,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (selectType) => (
                    <Tooltip placement="topLeft" title={selectType}>
                      {selectType}
                    </Tooltip>
                  ),
                },
                {
                  title: '表(table)',
                  dataIndex: 'table',
                  key: 'table',
                  width: 120,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (table) => (
                    <Tooltip placement="topLeft" title={table}>
                      {table}
                    </Tooltip>
                  ),
                },
                {
                  title: '分区(partitions)',
                  dataIndex: 'partitions',
                  key: 'partitions',
                  width: 120,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (partitions) => (
                    <Tooltip placement="topLeft" title={partitions}>
                      {partitions}
                    </Tooltip>
                  ),
                },
                {
                  title: '类型(type)',
                  dataIndex: 'type',
                  key: 'type',
                  width: 100,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (type) => (
                    <Tooltip placement="topLeft" title={type}>
                      {type}
                    </Tooltip>
                  ),
                },
                {
                  title: '可能用到的索引(possible_keys)',
                  dataIndex: 'possibleKeys',
                  key: 'possibleKeys',
                  width: 200,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (possibleKeys) => (
                    <Tooltip placement="topLeft" title={possibleKeys}>
                      {possibleKeys}
                    </Tooltip>
                  ),
                },
                {
                  title: '实际使用的索引(key)',
                  dataIndex: 'key',
                  key: 'key',
                  width: 150,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (key) => (
                    <Tooltip placement="topLeft" title={key}>
                      {key}
                    </Tooltip>
                  ),
                },
                {
                  title: '索引长度(key_len)',
                  dataIndex: 'keyLen',
                  key: 'keyLen',
                  width: 120,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (keyLen) => (
                    <Tooltip placement="topLeft" title={keyLen}>
                      {keyLen}
                    </Tooltip>
                  ),
                },
                {
                  title: '引用(ref)',
                  dataIndex: 'ref',
                  key: 'ref',
                  width: 120,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (ref) => (
                    <Tooltip placement="topLeft" title={ref}>
                      {ref}
                    </Tooltip>
                  ),
                },
                {
                  title: '扫描行数(rows)',
                  dataIndex: 'rows',
                  key: 'rows',
                  width: 120,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (rows) => (
                    <Tooltip placement="topLeft" title={rows}>
                      {rows}
                    </Tooltip>
                  ),
                },
                {
                  title: '过滤率(filtered)',
                  dataIndex: 'filtered',
                  key: 'filtered',
                  width: 120,
                  render: (filtered) => {
                    const text = `${filtered}%`;
                    const truncatedText = truncateText(text, 15);
                    return (
                      <Tooltip placement="topLeft" title={text}>
                        {truncatedText}
                      </Tooltip>
                    );
                  },
                },
                {
                  title: '额外信息(extra)',
                  dataIndex: 'extra',
                  key: 'extra',
                  width: 150,
                  ellipsis: {
                    showTitle: false,
                  },
                  render: (extra) => (
                    <Tooltip placement="topLeft" title={extra}>
                      {extra}
                    </Tooltip>
                  ),
                },
              ]}
              pagination={false}
              scroll={{ x: true }}
              size="small"
            />
          </Card>
        )}

        {analysisResult && (
          <Card
            title={
              <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
                <span>分析结果</span>
                <Tooltip title="复制分析结果">
                  <Button
                    type="text"
                    icon={<CopyOutlined />}
                    onClick={copyAnalysisResult}
                    size="small"
                  />
                </Tooltip>
              </div>
            }
            className={styles.resultCard}
          >
            {renderAnalysisResult(analysisResult)}
          </Card>
        )}
      </Card>

      {/* 数据源管理抽屉 */}
      <Drawer
        title="数据源管理"
        placement="right"
        width={720}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={showAddDataSourceModal}>
            添加数据源
          </Button>
        }
      >
        <Table
          dataSource={dataSources}
          columns={columns}
          rowKey="id"
          pagination={false}
          onRow={(record) => ({
            onClick: () => selectDataSource(record),
            style: { cursor: 'pointer' }
          })}
          rowClassName={() => styles.tableRow}
        />
        <div style={{ marginTop: 16, color: '#999', textAlign: 'center' }}>
          点击行可选择该数据源
        </div>
      </Drawer>

      {/* 添加/编辑数据源模态框 */}
      <Modal
        title={editingDataSource ? "编辑数据源" : "添加数据源"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={handleDataSourceFormSubmit}
        okText={editingDataSource ? "保存" : "添加"}
        cancelText="取消"
      >
        <Form
          form={dataSourceForm}
          layout="vertical"
        >
          <Form.Item
            name="name"
            label="数据源名称"
            rules={[{ required: true, message: '请输入数据源名称' }]}
          >
            <Input placeholder="请输入名称" />
          </Form.Item>
          <Form.Item
            name="url"
            label="数据库URL"
            rules={[{ required: true, message: '请输入数据库URL' }]}
          >
            <Input placeholder="例如: jdbc:mysql://localhost:3306/dbname" />
          </Form.Item>
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="用户名" />
          </Form.Item>
          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password placeholder="密码" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SqlAnalysis;
