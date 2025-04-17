import Footer from '@/components/Footer';
import type { RunTimeLayoutConfig } from '@umijs/max';
import defaultSettings from '../config/defaultSettings';
import { requestConfig } from './requestConfig';

export async function getInitialState(): Promise<InitialState> {
  return {
    currentUser: undefined,
  };
}

export const layout: RunTimeLayoutConfig = ({ initialState, setInitialState }) => {
  return {
    avatarProps: false,
    waterMarkProps: {
      content: 'SQL Analysis',
    },
    footerRender: () => <Footer />,
    menuHeaderRender: undefined,
    rightContentRender: false,
    ...defaultSettings,
  };
};

export const request = requestConfig;
