import twitter4j.*;
import java.util.ArrayList;
import java.util.Random;
import java.lang.String;

public class BotLotteryFunctions
{
    private Twitter twitter;                                //get twitter instance
    private Random randomGenerator = new Random();          //initialize "Random" class
    private ArrayList<Status> theList = new ArrayList<>();  //establish main ArrayList to hold the tweets
    private QueryResult theResult;                                  //establish QueryResult variable for use

    //constructor
    public BotLotteryFunctions()
    {
        //This method creates a Twitter instance
        twitter = TwitterFactory.getSingleton();
    }

    //query for retrieving correct tweets
    private void lotteryQuery()
    {
        Query query = new Query("lottery tickets");
        query.setCount(100);
        query.setMaxId(findLowestID());
        QueryResult result;
        try
        {
            result = twitter.search(query);
            theResult = result;
        } catch(TwitterException t)
        {
            String message = t.getErrorMessage();
            System.out.print("Error in method lotteryQuery: " + message);
        }
    }

    //use lotteryQuery to collect the needed amount of tweets in a single list
    public void gatherTweets()
    {
        int nearMaxAmount = 10000;    //set general number for amount of tweets wanted

        //loop to gather tweets until list is preferred size
        while (theList.size() < nearMaxAmount)
        {
            lotteryQuery();                             //call query each loop to refresh tweet results
            int temp = theList.size();                  //store current list size
            theList.addAll(theResult.getTweets());      //add new tweets

            System.out.print("\n\ncurrent size of list: " + theList.size() + "\n\n");

            //stop looping if query is no longer retrieving tweets
            if(temp == theList.size())
            {
                break;
            }

            //sleep 5 seconds each loop to prevent exceeding rate limit for twitter API
            try {
                Thread.sleep(5000);
            }catch(InterruptedException i){}
        }
    }

    //randomly select a winner out of the list
    public Status selectWinner()
    {
        //randomly pick index of list
        int index = randomGenerator.nextInt(theList.size());

        //Status variable to store winning tweet
        Status winner = theList.get(index);

        //check if tweet has won before, if so then remove the tweet and retry
        if(winner.isFavorited())
        {
            theList.remove(winner);
            selectWinner();
        }
        return winner;
    }

    //mention the winner and notify them of the odds they faced
    public void mentionWinner(Status winner)
    {
        //declare string for the mention
        String mention = "Congratulations @" + winner.getUser().getScreenName() + "! Because you tweeted about a lottery, your tweet " +
                "has been randomly selected out of " + theList.size() + " tweets as the winner of a tweet lottery. Your prize is a like and follow. Cool! ";
        //declare string variable to store link for quoting tweet
        String quoteLink = "https://twitter.com/" + winner.getUser().getScreenName()
                + "/status/" + winner.getId();

        //try block to quote the winning tweet, like the winning tweet, follow the winner, and handle Twitter exception
        try {
            //first line is for testing, next 3 are the methods for winner announcement
            //twitter.sendDirectMessage(*********, mention + quoteLink);
            twitter.updateStatus(mention + quoteLink);
            twitter.createFriendship(winner.getUser().getId());
            twitter.createFavorite(winner.getId());
        }catch(TwitterException t)
        {
            String message = t.getErrorMessage();
            System.out.print(message);
        }
    }

    //find lowest tweet ID of the current list of tweets
    private long findLowestID()
    {
        //initialize lowest ID as maximum possible long value
        //so that the query maxID will be set nonrestrictive if the list is empty
        long lowestID = Long.MAX_VALUE;

        for (Status tweet : theList)
        {
            lowestID = Math.min(tweet.getId(), lowestID);
        }
        return lowestID;
    }
}
