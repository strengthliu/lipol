/**
 * [省市联动js插件，需要area-data.js里的省市数据]
 * @return {[type]} [description]
 */
+(function(){
  /**
   * [description]
   * @param  {[type]} pEle [省 select的jq对象]
   * @param  {[type]} cEle [市 select的jq对象]
   * @param  {[type]} defaultProvince [默认显示省Id  默认北京]
   * @return {[type]}      [description]
   */
  var ProvinceCitySelects = function(pEle, cEle, defaultProvince) {
    this.pEle = pEle || $('#province')
    this.cEle = cEle || $('#city')
    this.defaultProvince = defaultProvince || '110000'
    this.provinceList = []
    this.cityList = []
    this.init()
    this.listenProvinceChange()
  }
  /**
   * [省市联动初始化]
   * @return {[type]} [description]
   */
  ProvinceCitySelects.prototype.init = function() {
    for (var pid in areaData) {
      this.pEle.append('<option value='+pid+'>'+areaData[pid].name+'</option>')
    }
    this.pEle.val(this.defaultProvince)
    this.getCitiesOptions(this.defaultProvince)
  }
  /**
   * [getCitiesOptions 根据选择省显示相应城市数组]
   * @type {[type]}
   */
   ProvinceCitySelects.prototype.getCitiesOptions = function(provinceId) {
     this.cEle.html('')
     var ciites = areaData[provinceId].child
     for (var cid in ciites) {
       this.cEle.append('<option value='+cid+'>'+ciites[cid].name+'</option>')
     }
   }
   /**
    * [listenProvinceChange 监听省select变化]
    * @return {[type]}            [description]
    */
   ProvinceCitySelects.prototype.listenProvinceChange = function() {
     var that = this
     var pEle = this.pEle
     pEle.on('change', function(e){
       var pid = e.target.value
       that.getCitiesOptions(pid)
     })
   }
  window.ProvinceCitySelects = ProvinceCitySelects
}())
