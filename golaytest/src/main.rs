extern crate cai_golay;
use cai_golay::extended::{encode,decode};


fn main() {

    let d = "110110100110";
    println!("original  {}",d);

    let data:u16 = u16::from_str_radix(d, 2).unwrap();
    //println!("{:?}",data);

    let mut w = encode(data);
    println!("encoded   {:b}",w);

    let e = "000000000000000000000101";
    println!("err       {}",e);
    let edata:u32 = u32::from_str_radix(e, 2).unwrap();

    w = w ^ edata;
    println!("modified  {:b}",w);

    let x = decode(w);
    if x.is_some() {
      println!("recovered {:b}",x.unwrap().0);
      println!("err cnt {}",x.unwrap().1);
    } else {
      println!("None");
    }

}

