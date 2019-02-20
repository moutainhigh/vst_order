package com.lvmama.vst.order.redis;


import java.util.List;
import java.util.Properties;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;


public class JedisTemplate2 {

	private static Logger logger = LoggerFactory.getLogger(JedisTemplate2.class);
	private Pool<Jedis> jedisPool;
	private static JedisTemplate2 readerInstance;
	private static JedisTemplate2 writerInstance;
	private static Object ReadLock = new Object();
	private static Object WriteLock = new Object();
	private static Properties properties = ConfigHelper.getProperties("redis2.properties");
	// redis是否启用
	private static boolean isRedisEnable = "true".equals(getValue("redis.enable"));

	public JedisTemplate2(Pool<Jedis> jedisPool) {
		this.jedisPool = jedisPool;
	}
	
	/**
	 * reader instance
	 *
	 * @return 如果配置了redis不启用，返回null
	 */
	public static JedisTemplate2 getReaderInstance(){
		if (!isRedisEnable) {
			return null;
		}
		if (readerInstance==null) {
			synchronized(ReadLock) {
				if (readerInstance==null) {
					String ip = getValue("redis.reader.server");
					int port = Integer.parseInt(getValue("redis.reader.port"));
					int maxIdle = Integer.parseInt(getValue("redis.reader.maxIdle"));
					int maxTotal = Integer.parseInt(getValue("redis.reader.maxTotal"));
					int checkingIntervalSecs = Integer.parseInt(getValue("redis.reader.checkingIntervalSecs"));
					int evictableIdleTimeSecs = Integer.parseInt(getValue("redis.reader.evictableIdleTimeSecs"));
					int maxWaitMillis = Integer.parseInt(getValue("redis.reader.maxWaitMillis"));
					
					JedisPoolConfig config = JedisUtils.createPoolConfig(maxIdle, maxTotal, checkingIntervalSecs, evictableIdleTimeSecs, maxWaitMillis);
					config.setTestWhileIdle(true);
					readerInstance = new JedisTemplate2(new JedisPool(config, ip, port));
				}
			}
		}
		return readerInstance;
	}

	public static String getValue(String key){
		if(StringUtils.isNotBlank(ZooKeeperConfigProperties.getProperties(key))){
			return ZooKeeperConfigProperties.getProperties(key);
		}
		String value = "";
		if(properties != null){
			value = properties.getProperty(key);
		}

		return value;
	}

	/**
	 * writer instance
	 *
	 * @return 如果配置了redis不启用，返回null
	 */
	public static JedisTemplate2 getWriterInstance(){
		if (!isRedisEnable) {
			return null;
		}
		if (writerInstance==null) {
			synchronized(WriteLock) {
				if (writerInstance==null) {
					String ip = getValue("redis.writer.server");
					int port = Integer.parseInt(getValue("redis.writer.port"));
					int maxIdle = Integer.parseInt(getValue("redis.writer.maxIdle"));
					int maxTotal = Integer.parseInt(getValue("redis.writer.maxTotal"));
					int checkingIntervalSecs = Integer.parseInt(getValue("redis.writer.checkingIntervalSecs"));
					int evictableIdleTimeSecs = Integer.parseInt(getValue("redis.writer.evictableIdleTimeSecs"));
					int maxWaitMillis = Integer.parseInt(getValue("redis.writer.maxWaitMillis"));

					JedisPoolConfig config = JedisUtils.createPoolConfig(maxIdle, maxTotal, checkingIntervalSecs, evictableIdleTimeSecs, maxWaitMillis);
					config.setTestWhileIdle(true);
			    	writerInstance = new JedisTemplate2(new JedisPool(config, ip, port));
				}
			}
		}
		return writerInstance;
	}
	
	/**
	 * 执行有返回结果的action。
	 */
	public <T> T execute(JedisAction<T> jedisAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			return jedisAction.action(jedis);
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	/**
	 * 执行无返回结果的action。
	 */
	public void execute(JedisActionNoResult jedisAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			jedisAction.action(jedis);
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	/**
	 * 根据连接是否已中断的标志，分别调用returnBrokenResource或returnResource。
	 */
	protected void closeResource(Jedis jedis, boolean connectionBroken) {
		if (jedis != null) {
			try {
				if (connectionBroken) {
					jedisPool.returnBrokenResource(jedis);
				} else {
					jedisPool.returnResource(jedis);
				}
			} catch (Exception e) {
				logger.error("Error happen when return jedis to pool, try to close it directly.", e);
				JedisUtils.closeJedis(jedis);
			}
		}
	}

	/**
	 * 获取内部的pool做进一步的动作。
	 */
	public Pool<Jedis> getJedisPool() {
		return jedisPool;
	}

	/**
	 * 有返回结果的回调接口定义。
	 */
	public interface JedisAction<T> {
		T action(Jedis jedis);
	}

	/**
	 * 无返回结果的回调接口定义。
	 */
	public interface JedisActionNoResult {
		void action(Jedis jedis);
	}

	// ////////////// 常用方法的封装 ///////////////////////// //

	// ////////////// 公共 ///////////////////////////
	/**
	 * 删除key, 如果key存在返回true, 否则返回false。
	 */
	public Boolean del(final String... keys) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.del(keys) == 1;
			}
		});
	}

	public void flushDB() {
		execute(new JedisActionNoResult() {

			@Override
			public void action(Jedis jedis) {
				jedis.flushDB();
			}
		});
	}

	// ////////////// 关于String ///////////////////////////
	/**
	 * 如果key不存在, 返回null.
	 */
	public String get(final String key) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}
	
	/**
	 * 获取通过clazz类型获取对象
	 * @param key
	 * @param clazz
	 * @return
	 */
	public <T> T get(final String key,final Class<T> clazz) {
		return execute(new JedisAction<T>() {
			@Override
			public T action(Jedis jedis) {
				String json = jedis.get(key);
				return JSON.parseObject(json, clazz);
			}
		});
	}
	
	/**
	 * 获取hash表中long值(如果value转换失败返回{@link Long.MIN_VALUE} )
	 * @param key
	 * @param field
	 * @return
	 */
	public  long hgetAsLong(final String key,final String field)
	{
		return execute(new JedisAction<Long>(){
			@Override
			public Long action(Jedis jedis) {
				// TODO Auto-generated method stub
				try {
					String value = jedis.hget(key, field);
					return Long.parseLong(value);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(),e);
					return Long.MIN_VALUE;
				}
			}
		});
	}
	
	/**
	 * 获取hash表String值
	 * @param key
	 * @param field
	 * @return
	 */
	public String hget(final String key,final String field)
	{
		return execute(new JedisAction<String>(){
			@Override
			public String action(Jedis jedis) {
				// TODO Auto-generated method stub
				return jedis.hget(key, field);
			}
		});
	}
	
	/**
	 * 获取hash表String值
	 * @param key
	 * @param field
	 * @return
	 */
	public <T> T hget(final String key,final String field,final Class<T> clazz)
	{
		return execute(new JedisAction<T>(){
			@Override
			public T action(Jedis jedis) {
				// TODO Auto-generated method stub
				String json = jedis.hget(key, field);
				return JSON.parseObject(json, clazz);
			}
		});
	}
	
	/**
	 * 设置key过期时间
	 * @param key
	 * @param seconds
	 * @return
	 */
	public boolean expire(final String key, final int seconds)
	{
		return execute(new JedisAction<Boolean>(){
			@Override
			public Boolean action(Jedis jedis) {
				// TODO Auto-generated method stub
				return jedis.expire(key, seconds) > 0;
			}
		});
	}
	
	/**
	 * 判断给定键值是否在redis缓存当中
	 * @param key
	 * @param field
	 * @return
	 */
	public boolean exists(final String key)
	{
		return execute(new JedisAction<Boolean>(){
			@Override
			public Boolean action(Jedis jedis) {
				// TODO Auto-generated method stub
				return jedis.exists(key);
			}
		});
	}
	
	
	/**
	 * 判断给定键值是否在redis缓存map当中
	 * @param key
	 * @param field
	 * @return
	 */
	public boolean hexists(final String key,final String field)
	{
		return execute(new JedisAction<Boolean>(){
			@Override
			public Boolean action(Jedis jedis) {
				// TODO Auto-generated method stub
				return jedis.hexists(key,field);
			}
		});
	}
	
	/**
	 * 添加hash键值对,如果添加失败,返回false(该方法如果键值存在会直接覆盖)
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public <T> boolean hset(final String key,final String field,final T value)
	{
		return execute(new JedisAction<Boolean>(){
			@Override
			public Boolean action(Jedis jedis) {
				// TODO Auto-generated method stub
				long count = jedis.hset(key, field, JSON.toJSONString(value));
				return count > 0;
			}
		});
	}
	
	/**
	 * 删除hash键值对
	 * @param key
	 * @param fields
	 * @return
	 */
	public boolean hdelete(final String key,final String... fields)
	{
		return execute(new JedisAction<Boolean>(){
			@Override
			public Boolean action(Jedis jedis) {
				// TODO Auto-generated method stub
				long count = jedis.hdel(key, fields);
				return count > 0;
			}
		});
	}

	/**
	 * 如果key不存在, 返回null.
	 */
	public Long getAsLong(final String key) {
		String result = get(key);
		return result != null ? Long.valueOf(result) : null;
	}

	/**
	 * 如果key不存在, 返回null.
	 */
	public Integer getAsInt(final String key) {
		String result = get(key);
		return result != null ? Integer.valueOf(result) : null;
	}

	public void set(final String key, final String value) {
		execute(new JedisActionNoResult() {

			@Override
			public void action(Jedis jedis) {
				jedis.set(key, value);
			}
		});
	}
	
	/**
	 * 
	 * @param key  
	 * @param value   
	 * @param seconds 过期时间,如果小于0则永不过期,单位为秒
	 */
	public void set(final String key, final Object value,final int...seconds) {
		execute(new JedisActionNoResult() {
			@Override
			public void action(Jedis jedis) {
				jedis.set(key, JSON.toJSONString(value));
				if(seconds.length > 0 && seconds[0] > 0)
				{
					jedis.expire(key, seconds[0]);
				}
			}
		});
	}

	public void setex(final String key, final String value, final int seconds) {
		execute(new JedisActionNoResult() {

			@Override
			public void action(Jedis jedis) {
				jedis.setex(key, seconds, value);
			}
		});
	}

	/**
	 * 如果key还不存在则进行设置，返回true，否则返回false.
	 */
	public Boolean setnx(final String key, final String value) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.setnx(key, value) == 1;
			}
		});
	}

	/**
	 * 综合setNX与setEx的效果。
	 */
	public Boolean setnxex(final String key, final String value, final int seconds) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				String result = jedis.set(key, value, "NX", "EX", seconds);
				return JedisUtils.isStatusOk(result);
			}
		});
	}

	public Long incr(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.incr(key);
			}
		});
	}

	public Long decr(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.decr(key);
			}
		});
	}

	public void lpush(final String key, final String... values) {
		execute(new JedisActionNoResult() {
			@Override
			public void action(Jedis jedis) {
				jedis.lpush(key, values);
			}
		});
	}

	public String rpop(final String key) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.rpop(key);
			}
		});
	}

	/**
	 * 返回List长度, key不存在时返回0，key类型不是list时抛出异常.
	 */
	public Long llen(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.llen(key);
			}
		});
	}

	/**
	 * 删除List中的第一个等于value的元素，value不存在或key不存在时返回false.
	 */
	public Boolean lremOne(final String key, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return (jedis.lrem(key, 1, value)==1);
			}
		});
	}

	/**
	 * 删除List中的所有等于value的元素，value不存在或key不存在时返回false.
	 */
	public Boolean lremAll(final String key, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return (jedis.lrem(key, 0, value) > 0);
			}
		});
	}

	// ////////////// 关于Sorted Set ///////////////////////////
	/**
	 * 加入Sorted set, 如果member在Set里已存在, 只更新score并返回false, 否则返回true.
	 */
	public Boolean zadd(final String key, final String member, final double score) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.zadd(key, score, member) == 1;
			}
		});
	}

	/**
	 * 删除sorted set中的元素，成功删除返回true，key或member不存在返回false。
	 */
	public Boolean zrem(final String key, final String member) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.zrem(key, member) == 1;
			}
		});
	}

	/**
	 * 当key不存在时返回null.
	 */
	public Double zscore(final String key, final String member) {
		return execute(new JedisAction<Double>() {

			@Override
			public Double action(Jedis jedis) {
				return jedis.zscore(key, member);
			}
		});
	}

	/**
	 * 返回sorted set长度, key不存在时返回0.
	 */
	public Long zcard(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.zcard(key);
			}
		});
	}

	// ////////////// 关于 List<Object> ///////////////////////////
	public <T> void setArray(final String key, final List<T> value, final int seconds) {
		if (isRedisEnable)
			this.setex(key,  JSON.toJSONString(value), seconds);
	}

	public <T> void setArray(final String key, final List<T> value) {
		if (isRedisEnable)
			this.set(key,  JSON.toJSONString(value));
	}

	/** key 不存在时返回 null */
	public <T> List<T> getArray(final String key, final Class<T> clazz) {
		if (!isRedisEnable)
			return null;
		return execute(new JedisAction<List<T>>() {
			@Override
			public List<T> action(Jedis jedis) {
				logger.info("jedis getArray(), key="+key);
				return JSON.parseArray(jedis.get(key), clazz);
			}
		});
	}
}
