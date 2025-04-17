import React, { useEffect, useState } from 'react';
import { Spin, Result, Button } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import styles from './index.less';
import { checkHealthUsingGet } from '@/services/backend/XmSoarController';

const SOAR_IFRAME_SRC = 'http://localhost:18889/webui';

const Soar: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const checkService = async () => {
    try {
      setLoading(true);
      setError(null);
      // 通过后端API检查服务状态
      const res = await checkHealthUsingGet()
      if (res.code === 200) {
        setLoading(false);
      } else {
        setError('Soar 服务响应异常');
      }
    } catch (error) {
      setError('无法连接到 Soar 服务');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkService();
  }, []);

  if (error) {
    return (
      <Result
        status="error"
        title="服务加载失败"
        subTitle={error}
        extra={[
          <Button
            key="retry"
            type="primary"
            icon={<ReloadOutlined />}
            onClick={checkService}
          >
            重试
          </Button>
        ]}
      />
    );
  }

  return (
    <div className={styles.container}>
      {loading ? (
        <div className={styles.loading}>
          <Spin size="large" />
          <p>正在加载 Soar 服务...</p>
        </div>
      ) : (
        <div className={styles.iframeContainer}>
          {/* 使用相对路径访问soar-web */}
          <iframe
            src={SOAR_IFRAME_SRC}
            className={styles.iframe}
            title="Soar Web UI"
            onLoad={() => setLoading(false)}
          />
        </div>
      )}
    </div>
  );
};

export default Soar; 