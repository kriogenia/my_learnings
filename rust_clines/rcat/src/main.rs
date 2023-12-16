use common::run_command;

mod cat;

fn main() {
    run_command::<cat::Cat>();
}
