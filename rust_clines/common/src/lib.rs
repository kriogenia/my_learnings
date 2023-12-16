use clap::Parser;

pub mod file;

pub type RunResult = Result<(), String>;

pub trait CommandClone {
    type Args: Parser;

    fn run() -> RunResult {
        let args = Self::Args::parse();
        Self::run_with_args(args)
    }

    fn run_with_args(args: Self::Args) -> RunResult;
}

pub fn run_command<T: CommandClone>() {
    if let Err(e) = T::run() {
        eprintln!("{}", e);
        std::process::exit(1)
    }
}
