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

// curl http://127.0.0.1:8000/ra -X POST -H 'Content-Type: application/json' -d '["1","2"]'
#[post("/ra",format="json",data="<message>")]
fn ra(message:Json<Vec<String>>) -> Json<Vec<User>> {
    let v:Vec<User> = message.to_vec().iter().map(|m| { 
      let id = m.to_string();
      let u = User { name: id.to_string(), status: get(id).unwrap() };
      u
    }).collect();
    Json(v)
}

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![r,s,ra])
}
