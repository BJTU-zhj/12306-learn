import { createRouter, createWebHistory } from 'vue-router'



const routes = [


  {
    path: '/',
    name: 'main',
    component: () => import('../views/MainView.vue'),
    children: [{
      path: 'welcome',
      component: ()=>import('../views/main/WelcomeView.vue')
    },
    {
      path: 'about',
      component: ()=>import('../views/main/AboutView.vue')
    },
      {
        path: 'station',
        component: ()=>import('../views/main/StationView.vue')
      },
      {
        path: 'train',
        component: ()=>import('../views/main/TrainView.vue')
      },
      {
        path: 'train-station',
        component: ()=>import('../views/main/TrainStationView.vue')
      }
    ]
  },
  {
    path: '',
    redirect: '/welcome'
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
