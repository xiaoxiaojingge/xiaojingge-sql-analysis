export default [
  {
    path: '/',
    redirect: '/sql-analysis',
  },
  {
    path: '/',
    name: 'SQL工具',
    flatMenu: true,
    routes: [
      {
        path: '/sql-analysis',
        name: '京东SQL分析',
        icon: 'DatabaseOutlined',
        component: './SqlAnalysis',
      },
      {
        path: '/sql-soar',
        name: '小米Soar',
        icon: 'CodeOutlined',
        component: './SqlSoar',
      },
    ],
  },
  {
    path: '*',
    layout: false,
    component: './404',
  },
];
