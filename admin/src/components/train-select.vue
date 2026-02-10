<template>
  <a-select
      v-model:value="trainCode"
      show-search
      allow-clear
      :filter-option="filterTrainCodeOption"
      placeholder="请选择车次"
      @change="onChange"
  >
    <a-select-option
        v-for="item in trains"
        :key="item.code"
        :value="item.code"
        :label="item.code + item.start + item.end"
    >
      {{ item.code }} | {{ item.start }} ~ {{ item.end }}
    </a-select-option>
  </a-select>
</template>

<script>
import { defineComponent, ref, onMounted, watch } from 'vue';
import axios from "axios";
import { notification } from "ant-design-vue";

export default defineComponent({
  name: "TrainSelectView",
  props: ["value"], // 接收父组件传入的值
  emits: ["update:value", "change"], // 定义向外抛出的事件
  setup(props, { emit }) {
    const trainCode = ref();
    const trains = ref([]);

    // 监听父组件传入的值变化，同步给内部的 trainCode
    watch(() => props.value, () => {
      trainCode.value = props.value;
    }, { immediate: true });

    /**
     * 查询所有车次
     */
    const queryTrainCode = () => {
      axios.get("/business/admin/train/query-all").then((response) => {
        let data = response.data;
        if (data.success) {
          trains.value = data.content;
        } else {
          notification.error({ description: data.message });
        }
      });
    };

    /**
     * 搜索过滤：匹配 label 字段
     */
    const filterTrainCodeOption = (input, option) => {
      return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    };

    /**
     * 当选择发生变化时，通知父组件
     */
    const onChange = (value) => {
      emit("update:value", value);
      let train=trains.value.filter(item=>item.code===value)[0];
      if(Tool.isEmpty( train)){
        train={};
      }
      emit("change", train);
    };

    onMounted(() => {
      queryTrainCode();
    });

    return {
      trainCode,
      trains,
      filterTrainCodeOption,
      onChange
    };
  },
});
</script>