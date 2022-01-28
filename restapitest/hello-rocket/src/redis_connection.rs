use r2d2;
use r2d2_redis::RedisConnectionManager;

use r2d2::{Pool,PooledConnection};

pub type RedisPool = r2d2::Pool<RedisConnectionManager>;
pub type RedisPooledConn = PooledConnection<RedisConnectionManager>;

pub struct MyPool(pub RedisPool);

pub fn init_pool() -> MyPool {
    let manager = RedisConnectionManager::new(format!("redis://{}:{}/{}", "localhost", "6379", "0")).expect("connection manager");
    MyPool(Pool::builder().build(manager).unwrap())
}

