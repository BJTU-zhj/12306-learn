import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Antd, {notification} from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import * as Icons from '@ant-design/icons-vue'
import axios from "axios";
import './assets/js/enums';

//axios拦截器
axios.interceptors.request.use( function (config){
  console.log("请求参数", config);
  const token=store.state.member.token;
  if(token){
      config.headers.token=token;
      console.log("添加token:",token);
  }
  return config;
}, function (error) {
    return Promise.reject(error);
})

axios.interceptors.response.use( function (response){
  console.log("返回参数", response);
  return response;
}, function (error) {
    console.log("返回错误", error);
    const response = error.response;
    const status=response.status;
    if(status===401){
        console.log("未登录或登录失效，跳转到登录页");
        store.commit("setMember", {})
        notification.error({

            description: '未登录获登录失效'
        });
        router.push('/login')
        // 返回一个空的 Promise，不再继续 reject 抛错
        return new Promise(() => {});
    }
    return Promise.reject(error);
})

axios.defaults.baseURL = process.env.VUE_APP_SERVER;
console.log('环境：', process.env.NODE_ENV);
console.log('服务端：', process.env.VUE_APP_SERVER);

const app=createApp(App);
app.use(Antd).use(store).use(router).mount('#app');

//全局使用图标
const icons = Icons;
for (const i in icons) {
  app.component(i, icons[i]);
}
