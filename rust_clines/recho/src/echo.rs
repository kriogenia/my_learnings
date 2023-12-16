use clap::Parser;
use common::CommandClone;

#[derive(Debug, Parser)]
#[command(author, version, about)]
pub struct Args {
    #[arg(required = false, help = "Input text")]
    text: Option<Vec<String>>,
    #[arg(default_value = "false", short = 'n', help = "Do not print newline")]
    omit_newline: bool,
}

pub struct Echo;

impl CommandClone<Args> for Echo {
    fn run_with_args(args: Args) -> common::RunResult {
        print!(
            "{}{}",
            args.text.unwrap_or(vec![]).join(" "),
            if args.omit_newline { "" } else { "\n" }
        );
        Ok(())
    }
}
