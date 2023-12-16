use common::CommandClone;

mod cat;

fn main() {
    if let Err(e) = cat::Cat::run() {
        eprintln!("{}", e);
        std::process::exit(1)
    }
}
