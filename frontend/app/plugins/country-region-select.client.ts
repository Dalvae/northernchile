import { CountrySelect, RegionSelect } from 'vue3-country-region-select';

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.component('CountrySelect', CountrySelect);
  nuxtApp.vueApp.component('RegionSelect', RegionSelect);
});
