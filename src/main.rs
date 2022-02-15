use teloxide::prelude2::*;

#[tokio::main]
async fn main() {
    teloxide::enable_logging!();
    log::info!("Starting dices_bot...");

    let bot = Bot::from_env().auto_send();

    teloxide::repls2::repl(bot, |message: Message, bot: AutoSend<Bot>| async move {
        if message.text() == Some("conquista") {
            bot.send_message(message.chat.id, "Meu objetivo é a conquista!!!").await?;
        } else if message.text() == Some("dado") {
            bot.send_dice(message.chat.id).await?;
        }
        respond(())
    })
    .await;
}
