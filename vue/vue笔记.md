# vue笔记

官方文档：https://cn.vuejs.org/v2/guide/conditional.html



## 使用

1. 使用`<script>`标签引入vue.js。
2. 创建全局变量 

```
var vm = new Vue({
	el: "#app",
	data: {
		message: "hello vue"
	}
})
```

data中声明的属性会加入到 Vue 的**响应式系统**中，实现数据和视图的双向绑定。

这些属性和vue本身暴露出来的属性和方法，可以使用$来引用，且和用户自定义的区分开来。

```javascript
vm.$data.message
vm.$wathch('message',function(newValue,oldValue){
    ...
})
```



## 生命周期

vue提供给用户在不同阶段运行用户代码的钩子，有创建前、创建后，挂载前，挂载后、更新前、更新后、组件触发前，触发后……（见vue API 部分）

<img src="https://cn.vuejs.org/images/lifecycle.png" alt="Vue 实例生命周期" style="zoom: 50%;" />

## 模板语法

具体见官网模板语法，这里只是记录有哪些语法。

### 插值

* `{{}}`用于`html`中的内容数据绑定，可以使用`javascript运算`：

  * 基本运算：`{{number + 1}}`
  * 三目运算：`{{1>3 ? 'yes' : `no`}}`
  * 方法调用：`{{  message.split('').reverse().join('') }}`
  * 流程控制、语句不能生效。比如：

  ```
  <!-- 这是语句，不是表达式 -->
  {{ var a = 1 }}
  
  <!-- 流控制也不会生效，请使用三元表达式 -->
  {{ if (ok) { return message } }}
  ```

  

* v-once： 只展示一次修改

* v-html：插入html标签内容。

* v-bind： 绑定数据、样式。因为`{{}}`语法不能用在`html`标签的属性上，所以使用`v-bind`来绑定值到属性上。

  * 和`{{}}`语法类似，可以进行基本的`javascript`运算，比如：
* `<div v-bind:id="'list-' + id"></div>` 
  * 动态语法：`v-bind:[参数]=‘’`，简写为：`:[参数]=''`

* v-model：表单输入和应用状态之间的双向绑定。

```
<div id="app-6">
  <p>{{ message }}</p>
  <input v-model="message">
</div>
```



### 指令

```
<p v-if="seen">现在你看到我了</p>

<a v-bind:href="url">...</a>

<a v-on:click="doSomething">...</a>
```

2.6.0后增加动态参数：

```html
<a v-bind:[attributeName]="url"> ... </a>
缩写：
    <!-- 缩写 -->
    <a :href="url">...</a>

    <!-- 动态参数的缩写 (2.6.0+) -->
    <a :[key]="url"> ... </a>

<a v-on:[eventName]="doSomething"> ... </a>
缩写：
    <!-- 缩写 -->
    <a @click="doSomething">...</a>

    <!-- 动态参数的缩写 (2.6.0+) -->
    <a @[event]="doSomething"> ... </a>

约束：不能有空格和表达式，有的应该使用计算属性来替代。
```

## 组件

组件是可复用的 Vue 实例，可以自定义名字、属性、模板 ，与一个vue实例接收的参数基本一致。比如：`data`、`computed`、`watch`、`methods` 以及生命周期钩子等。除了`el`(element)是vue根节点特有的。

```vue
Vue.component('button-counter', {
  props: ['title'],
  data: function () {
    return {
      count: 0
    }
  },
  template: '<button v-on:click="count++">You clicked me {{ count }} times.{{title}}</button>'
})
```

```html
<div id="components-demo">
  <button-counter title="my title"></button-counter>
</div>
```

```javascript
new Vue({ el: '#components-demo' })
```

注意点：

* data是一个函数，不是vue中的对象`data:{...}`，用于让每个组件维护一套独立的变量拷贝。

* 注册：

Vue.component 方式是全局注册，在其中注册的组件，在其他的vue实例中可以直接使用。在vue实例内部注册的组件为局部组件：

```javascript
new vue{
	components: {}
}
```

* 自定义属性：

在props中指定自定义属性，可以在使用组件时从外部传入，如上面的`title`，在使用组件时可以接受传入的动态数据。

自定义属性也可以是一个对象，让模板接收一个对象后，使用对象的属性。比如`blog`：

```vue
<blog-post
  v-for="blog in blogs"
  v-bind:key="blog.id"
  v-bind:post="blog"
></blog-post>
```

```vue
Vue.component('blog-post', {
  props: ['blog'],
  template: `
    <div class="blog-post">
      <h3>{{ blog.title }}</h3>
      <div v-html="blog.content"></div>
    </div>
  `
})

```

**监听子组件事件**

子组件模板中设置的事件，想要通知父级组件的话，需要使用`$emit`方法，这样当子事件被触发时，可以通知上去。

见`教程 -> 组件基础 -> 监听子组件事件`中的示例。





东西很多，直接看官网吧

