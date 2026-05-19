<script setup lang="ts">
import { provideGlobalConfig } from 'element-plus/es/components/config-provider/src/hooks/use-global-config.mjs'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

provideGlobalConfig({ locale: zhCn })
</script>

<template>
  <router-view />
</template>
