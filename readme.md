#前端BUG
##修改书籍
1 修改书籍图片加载可能加载失败

2 上传书籍封面组件未与书籍封面大小保持一致

3 上传书籍文件没有显示原有的书籍文件


#后端
1 文件存放的地址不必使用UUID，可以使用时间加文件名的格式存储

2 借阅信息可以再增加一个借阅详情表，实现借阅时间线

3 电子书可以新增书签功能，阅读记录存入record表中

4 用户修改信息不能为空，sql语句会对实体进行判断
