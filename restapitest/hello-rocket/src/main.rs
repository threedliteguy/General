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


#[get("/")]
fn a() -> Json<User> {
    let _x = fetch_an_integer().ok().unwrap();
    let u = User { name: "a".to_string(), status: _x };
    Json(u)
}


fn fetch_an_integer() -> redis::RedisResult<String> {
    let client = redis::Client::open("redis://127.0.0.1/")?;
    let mut con = client.get_connection()?;
    let _ : () = con.set("my_key", 42)?;
    con.get("my_key")
}

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![a])
}
