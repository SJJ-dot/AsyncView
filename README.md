# AsyncView [![](https://jitpack.io/v/SJJ-dot/AsyncView.svg)](https://jitpack.io/#SJJ-dot/AsyncView)

异步加载view。相比官方的，增加了一些可选参数。实际使用中发现xml加载线程会被中断，使用了java的单线程线程池。
```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```groovy

dependencies {
	        implementation 'com.github.SJJ-dot:AsyncView:1.0.1'
	}

```
### Simple
```
setContentView(AsyncView(this,R.layout.activity_async){view->
	//init view 。返回的view始终是加载上来的xml。所以异步加载不支持merge标签
})
```
