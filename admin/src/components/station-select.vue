<template>
  <a-select
      v-model:value="trainStation"
      show-search
      allow-clear
      :filter-option="filterTrainStationOption"
      placeholder="请选择车站"
      @change="onChange"
  >
    <a-select-option
        v-for="item in stations"
        :key="item.name"
        :value="item.name"
        :label="item.name + item.namePinyin + item.namePy"
    >
      {{ item.name }} | {{ item.namePinyin }} ~ {{ item.namePy }}
    </a-select-option>
  </a-select>
</template>

<script>
import { defineComponent, ref, onMounted, watch } from 'vue';
import axios from "axios";
import { notification } from "ant-design-vue";

export default defineComponent({
  name: "TrainStationSelectView",
  props: ["value"], // 接收父组件传入的值
  emits: ["update:value", "change"], // 定义向外抛出的事件
  setup(props, { emit }) {
    const trainStation = ref();
    const stations = ref([]);

    // 监听父组件传入的值变化，同步给内部的 trainStation
    watch(() => props.value, () => {
      trainStation.value = props.value;
    }, { immediate: true });

    /**
     * 查询所有车站
     */
    const queryTrainStation = () => {
      axios.get("/business/admin/station/query-all").then((response) => {
        let data = response.data;
        if (data.success) {
          stations.value = data.content;
        } else {
          notification.error({ description: data.message });
        }
      });
    };

    /**
     * 搜索过滤：匹配 label 字段
     */
    const filterTrainStationOption = (input, option) => {
      return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    };

    /**
     * 当选择发生变化时，通知父组件
     */
    const onChange = (value) => {
      emit("update:value", value);
      let station=stations.value.filter(item=>item.code===value)[0];
      if(Tool.isEmpty( station)){
        station={};
      }
      emit("change", station);
    };

    onMounted(() => {
      queryTrainStation();
    });

    return {
      trainStation,
      stations,
      filterTrainStationOption,
      onChange
    };
  },
});
</script>