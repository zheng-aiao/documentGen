基于 swagger2/3 框架 在线文档生成器，支持生成word/pdf

# 二种使用方式
1，公司使用
- （1）需提供一个该公司所有基于swagger框架开发的微服务信息的swagger json数据集合获取接口地址，返回数据结构要包含 服务名和url信息。
- （2）在本产品前端界面，设置按钮弹窗处配置 name字段（服务名） ，url字段（swagger json数据url）对应的返回数据结构字段
- （3）本产品会根据配置的地址和解析字段，展示所有的服务信息， 并会探测swagger json的url地址是否可访问，会在状态列标出 正常/异常

<img width="2553" height="1177" alt="QQ_1759076029091" src="https://github.com/user-attachments/assets/24f03a31-1bb5-4cc7-9a12-6e9e70779bf1" />

<img width="2559" height="1182" alt="QQ_1759075546858" src="https://github.com/user-attachments/assets/9daa6255-a65b-4f25-bbc0-25a84ab75718" />

2. 个人使用：
- 使用方式：：（1）上传json文件 （2）填入swagger json可访问到的数据的地址
- 功能优点：   可按需选择需要生成的接口
