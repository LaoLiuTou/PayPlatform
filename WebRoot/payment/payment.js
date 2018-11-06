 
var app = getApp()
var url ='http://192.168.1.144/PayPlatform/';
//var url = 'http://zhifu.jiubaisoft.com/PayPlatform/';
Page({
  data: { total_fee: '0.01' },
  onLoad: function () {
    
  },
  requestPayment: function () {
    var that = this

    that.setData({
      loading: true
    })

    wx.login({
      success(res) {
        if (res.code) {
          var code = res.code;
          console.log('返回res.code:' + code)
          //发起网络请求 
        
          wx.request({
            method: 'POST', 
            url: url+'getOpenId',
            
            header: {
              "Content-Type": "application/x-www-form-urlencoded"
            },
            data: { 'code': code},
            success: function (res) { 
              console.log(JSON.parse(res.data)) 
              that.generateOrder(JSON.parse(res.data).openid);
            }
          })

        } else {
          console.log('登录失败！' + res.errMsg)
        }
      }
    })
   
  } ,

   /**生成商户订单 */ 
  generateOrder: function (openid) {
    var that = this
      var fee = Number(that.data.total_fee)*100
    //统一支付 
    wx.request({
      url: url +'wxpay',
      
      method: 'POST',
      data: {
        total_fee: fee,
        body: '支付测试',
        openid: openid,
        out_trade_no: '20181102000002'

      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      success: function (res) {
        console.log(res)
        var payargs = res.data
        //发起支付  
        that.pay(payargs)
      },
    })
  },
  /* 支付   */
  pay: function (payargs) {
    console.log("支付") 
    console.log(payargs);
    wx.requestPayment({ 
      timeStamp: payargs.timeStamp+"",
      nonceStr: payargs.nonceStr,
      package: payargs.package,
      signType: payargs.signType,
      paySign: payargs.paySign,

      
      success: function (res) {
        // success 
        console.log(res)
        wx.navigateBack({
          delta: 1, // 回退前 delta(默认为1) 页面 
          success: function (res) {
            wx.showToast({
              title: '支付成功',
              icon: 'success',
              duration: 2000
            })
          },
          fail: function () {
            // fail 
          },
          complete: function () {
            // complete 
          }
        })
      },
      fail: function (res) {
        // fail 
        console.log("支付失败")
        console.log(res)
      },
      complete: function () {
        // complete 
        console.log("pay complete")
      }
    }) 
  }
})
