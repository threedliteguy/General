#[macro_use] extern crate rocket;
extern crate redis;
use redis::Commands;

use serde::{Serialize, Deserialize};
use rocket::serde::json::Json;

#[derive(Serialize, Deserialize, Debug)]
struct User {
    name: String,
    status: String 
}

fn getcon() -> Result<redis::Connection,redis::RedisError> {
    let client = redis::Client::open("redis://127.0.0.1/")?;
    client.get_connection()
}

fn get(id:String) -> Result<String,redis::RedisError> {
    let x:Result<String,redis::RedisError> = getcon()?.get(id);
    x.or(Ok("".to_string()))
}
fn set(id:String,val:String) -> Result<(),redis::RedisError> {
    getcon()?.set(&id,val)
}

#[get("/r/<id>")]
fn r(id:String) -> Json<User> {
    let u = User { name: id.to_string(), status: get(id).unwrap() };
    Json(u)
}

// s/b post
#[get("/s/<id>?<val>")]
fn s(id:String, val:String) -> Json<User> {
    set(id.to_string(),val).unwrap();
    r(id)
}


#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![r,s])
}
