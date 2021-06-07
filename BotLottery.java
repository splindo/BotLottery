import twitter4j.*;

public class BotLottery {

    public static void main(String args[])
    {
        BotLotteryFunctions BLF = new BotLotteryFunctions();
        BLF.gatherTweets();
        Status winner = BLF.selectWinner();
        BLF.mentionWinner(winner);
    }
}
