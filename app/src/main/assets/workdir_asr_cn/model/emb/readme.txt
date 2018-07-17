### files:

1. 动态batch，动态rnn，nhwc ，这是单路ctc用的模型: emb_xyxy/
2. 跟1一样，换成nchw格式，这是给Kamino用的模型: emb_xyxy_nchw/
3. 动态batch，静态rnn，nhwc，这是给TFLite的版本: emb_xyxy_batch0_begin/ emb_xyxy_batch0_continue/


####
更新20180611_xiaoyaxiaoya:
	1、增加了两批线上加噪声数据
	ASR-L-023-1_rokid_online_p1.ordinarynoise
	ASR-L-023-1_rokid_online_p2.ordinarynoise
	2、将变速1.1倍的晓雅晓雅数据减少到5万

更新20180611_150000ruoqi:
	1、增加加噪声的若琪5万句
	ASR-L-023-1_rokid_online_p1_25000ruoqi.ordinarynoise
	ASR-L-023-1_rokid_online_p2_25000ruoqi.ordinarynoise
	2、增加的带噪声的不带若琪的线上数据两批分别是：
	ASR-L-023-1_rokid_online_p1_other.ordinarynoise
	ASR-L-023-1_rokid_online_p2_other.ordinarynoise

更新20180607_xiaoya1.2:
	去掉加噪声的数据

更新20180609_rokid200000:
	1、ASR-L-023-1_rokid_online减少到20万
	2、ASR-L-023-1_rokidonline_195500ruopi    195500语句
	3、ASR-L-023-1_rokidonline_ruoqi  4500句若琪

更新20180609_rokid100000:
	1、ASR-L-023-1_rokid_online减少到10万
	2、ASR-L-023-1_rokidonline_95500ruoqi    95500语句
	3、ASR-L-023-1_rokidonline_ruoqi  4500句若琪

更新20180608_xiaoya1.2:
	1、激活词只用了晓雅晓雅
        2、晓雅晓雅进行了1.2倍变速

更新20180608_xiaoya1.1:
	1、激活词只用了晓雅晓雅
	2、晓雅晓雅进行了1.1倍变速

更新20180602_xiaoya1.2jiasu:
	增加了小雅小雅提速1.2倍的语料，具体如下：
	ASR-S-001-3_xiaoyaxiaoya_tempo20
	ASR-S-001-2_xiaoyaxiaoya_tempo20
	ASR-S-001-1_xiaoyaxiaoya_tempo20
	ASR-S-001-6_xiaoyaxiaoya_tempo20
	ASR-S-001-4_xiaoyaxiaoya_tempo20
	ASR-S-001-5_xiaoyaxiaoya_tempo20

更新20180530_xiaoyaNoise:
	增加了带噪声的激活词小雅小雅语料和小雅迷你长句：
	a)ASR-L-006-1_xiaoyaxiaoya0413asr.ordinarynoise
	b)ASR-S-001-1_ximalaya.ordinarynoise
	c)ASR-S-001-2_tob_xiaoyaxiaoya.ordinarynoise
	d)ASR-S-001-5_xiaoyaxiaoya0402.ordinarynoise
	e)ASR-S-001-6_xiaoyaxiaoya07-15.ordinarynoise

更新20180528_xiaoyabiansu:
	新增数据：
	ASR-S-001-1_xiaoyaxiaoya_tempo10
	ASR-S-001-2_xiaoyaxiaoya_tempo10
	ASR-S-001-3_xiaoyaxiaoya_tempo10
	ASR-S-001-4_xiaoyaxiaoya_tempo10
	ASR-S-001-5_xiaoyaxiaoya_tempo10
	ASR-S-001-6_xiaoyaxiaoya_tempo10
	ASR-S-022-1_rokidonline20180101-052
	另外，将涂鸦智能和小咚小咚数据换为单通道

更新20180525_xiaoya:
	激活词晓雅晓雅和嘀嗒嘀嗒只使用单通道数据

更新20180522_vehicle:
	对以下数据添加了车载噪声:
		number_panasonic.carnoise
		rokid_record.carnoise
		wx_record_ht.carnoise
		xiaoyaxiaoya0402.carnoise
		ximalaya.carnoise
		tobSecond_xiaoyaxiaoya.carnoise
		number_201405_onlyNumber.carnoise
		rokid_online.carnoise
		caiyin_chn_201206.carnoise

	使用了所有的车载录音数据:
		chezaixiaoya_p1
		chezaixiaoya_p2
		chezaixiaoya_p3

	增加了xiaoyaxiaoya非车载数据:
		xiaoyaxiaoya_p0
		ximalaya
		tobSecond
		xiaoyaxiaoya_20180424
		xiaoyaxiaoya_20180423
		xiaoyaxiaoya0413asr
		xiaoyaxiaoya0402

更新20180516_alexa:
	american
	ami
	caiyin_blcu_eng_201212
	caiyin_eng_201205
	king_asr
	libri
	melody
	rokid_english_all.s.t
	ShujutangFreeSeptember
	sw
	tatoeba
	ted
	vox
	wsj

更新20180511:
	1、整理了数据,把以下数据添加了进来,目前所有的激活词，统一到了一个模型中：
	   wangyi/20180325/duolami
	   haimeidi/20180403
	   wangyi/20180404/duolami
	   wangyi/20180404/didadida
	   wangyi/20180325/didadia
	   一共增加了377988句

更新20180510:
	1、删除了小雅小雅强制对齐中对不齐的数据10221条

更新20180504：
	1、增加了3.5万句的滴答滴答录音
	2、新增了5万句涂鸦智能录音

更新20180425：
    1、在20180415模型的基础上，增加了6批数据：
        5万句小咚小咚
        1万句小洲小洲
        3.4万句小雅小雅
        2.2万句滴答滴答
        0.2万句海美迪
    
更新20180415:
    1、在20180412模型的基础上，增加了26000句的长句录音。
    2、为了快速跑完模型，大于10s的语音暂时先抛弃掉了，后面重新用上。
更新20180412：
    1、在20180403noBF模型的基础上，去掉了垃圾数据
    2、垃圾数据是通过程序筛选出来的。具体是通过强制对齐，哪些截断的语音对不齐，然后会被丢弃。

###

#### 测试建议 
20180611_xiaoyaxiaoya:
对激活词晓雅晓雅进行测试

20180611_150000ruoqi:
对线上数据进行测试

20180607_xiaoya1.2:
对激活词晓雅晓雅进行测试

20180609_rokid200000:
对线上数据进行测试

20180609_rokid100000:
对线上数据进行测试

20180608_xiaoya1.2:
对晓雅晓雅进行测试

20180608_xiaoya1.1:
对晓雅晓雅进行测试

20180602_xiaoya1.2jiasu:
对晓雅晓雅进行测试

20180530_xiaoyaNoise:
对激活词晓雅晓雅进行测试

20180528_xiaoyabiansu:
对激活词晓雅晓雅、若琪、涂鸦智能和小咚小咚进行测试

20180525_xiaoya:
对晓雅晓雅和嘀嗒嘀嗒进行测试

20180522_vehicle:
对车载激活进行测试

20180516_alexa:
对激活词“Alexa”的性能进行测试
        
20180511:
1、对激活词“嘀嗒嘀嗒”的性能进行测试
2、对激活词“哆啦咪”的性能进行测试
3、对激活词“你好小薇”的性能进行测试
4、对激活词“嗨小薇”的性能进行测试
5、对激活词“嘿小薇”的性能进行测试

20180510:
1、对激活词“小雅小雅”的性能进行测试

20180504:
1、对激活词“滴答滴答”的性能进行测试
2、对激活词“涂鸦智能”的性能进行测试
