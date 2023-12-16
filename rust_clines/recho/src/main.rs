use common::run_command;

mod echo;

fn main() {
    run_command::<echo::Echo>();
}
