# AsyncInflateView
异步加载view 。增加了loading。
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
	        implementation 'com.github.SJJ-dot:AsyncInflateView:1.0.0'
	}

```
### Simple
```
setContentView(AsyncView(this,R.layout.activity_async){

})
```