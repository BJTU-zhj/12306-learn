<template>
  <a-row class="login" >
    <a-col :span="8" :offset="0" class="login-main">
      <h1 style="text-align: center"><rocket-two-tone />&nbsp;抢的快12306售票系统</h1>
      <a-form
          :model="loginForm"
          name="basic"
          autocomplete="off"
      >
        <a-form-item
            label=""
            name="mobile"
            :rules="[{ required: true, message: '请输入手机号!' }]"
        >
          <a-input v-model:value="loginForm.mobile" placeholder="手机号"/>
        </a-form-item>

        <a-form-item
            label=""
            name="code"
            :rules="[{ required: true, message: '请输入验证码!' }]"
        >
          <a-input v-model:value="loginForm.code">
            <template #addonAfter>
              <a @click="sendCode">获取验证码</a>
            </template>
          </a-input>
          <!--<a-input v-model:value="loginForm.code" placeholder="验证码"/>-->
        </a-form-item>

        <a-form-item>
          <a-button type="primary" block @click="login">登录</a-button>
        </a-form-item>

      </a-form>
    </a-col>
  </a-row>
</template>

<script>
import { defineComponent, reactive } from 'vue';
import axios from 'axios';

export default defineComponent({
  name: "login-view",
  setup() {

    const loginForm = reactive({
      mobile: '13000000000',
      code: '',
    });

    const sendCode = ()=>{
      axios.post('http://localhost:8000/member/member/sendcode',{mobile:loginForm.mobile}).then(response=>{console.log(response)})
    };

    return {
      loginForm,
      sendCode,
    };
  },
});
</script>

<style scoped>
.login {
  /* 让这一行占满整个浏览器可视区域的高度 */
  height: 100vh;

  /* 开启 Flex 布局 */
  display: flex;

  /* 水平居中 */
  justify-content: center;

  /* 垂直居中 */
  align-items: center;

  /* 可选：加个背景图或底色 */
  background-color: #f0f2f5;
}

.login-main {
  /* 设置一个白色背景框，让登录区域更明显 */
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
