<template>
  <div>{{ dailyTrainTicket }}</div>
</template>
<script>
import { defineComponent } from 'vue';

export default defineComponent({
  name:"order-view",
  setup() {
    const dailyTrainTicket=sessionStorage.getItem("dailyTrainTicket")||{};
    console.log("下单的车次信息：", dailyTrainTicket.valueOf());
    return {
      dailyTrainTicket
    };
  },
});
</script>
<style>
</style>
