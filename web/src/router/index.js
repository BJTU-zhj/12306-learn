import { createRouter, createWebHistory } from 'vue-router'


const routes = [

  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/',
    name: 'main',
    component: () => import('../views/MainView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
