package wp.anthony.client;

import wp.anthony.ConsoleHelper;
import wp.anthony.Message;
import wp.anthony.MessageType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {
    protected SocketThread getSocketThread(){
        return new BotSocketThread();
    }

    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    protected String getUserName(){

        return "date_bot_" + (int) (Math.random() * 100);
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
    public class BotSocketThread extends SocketThread{

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
            String[] split = message.split(": ");
            if (split.length != 2) return;

            String messageWithoutUserName = split[1];

            String format = null;

            switch (messageWithoutUserName){
                case "дата":
                    format = "d.MM.YYYY";
                    break;
                case "день":
                    format = "d";
                    break;
                case "месяц":
                    format = "MMMM";
                    break;
                case "год":
                    format = "YYYY";
                    break;
                case "время":
                    format = "H:mm:ss";
                    break;
                case "час":
                    format = "H";
                    break;
                case "минуты":
                    format = "m";
                    break;
                case "секунды":
                    format = "s";
                    break;
            }

            if (format != null){
                String result = new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
                BotClient.this.sendTextMessage("Информация для " + split[0] + ": " + result);
            }
        }
    }
}
