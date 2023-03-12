public class J{
  
    // main函数的拼写都是小写的，大小写敏感
    public static void main(String[] args){
        System.out.println("HelloWorld!");
        int team = 0, game = 8;
        System.out.println("Input team: ");
        String[]teams={"Lakers","Warriors","Bulls","Suns","Clippers"};
        //the number of the game.
        String[]games={"Game 8","Game 9","Game 10","Game 11","Game 12"};
        //input the scores for each team.
         int[][]score= 
        {   {128,99,101,120,115,130,150,80,77,100,110,125},
            {90,100,150,145,130,99,120,125,110,95,150,113},
            {102,105,135,99,121,137,163,121,98,178,156,130},
            {110,101,100,110,80,120,150,151,145,96,80,77},
            {140,125,128,110,100,90,98,95,130,140,135,105}};
         //input sum and avg;
       int sum,avg;
       //if the team is at 0, the team numbers will not go over 6.
       for (team=0;team<score.length;team++){
            //print the team name.
            System.out.println(teams[team]);
            //sum is equal to 0.
            sum=0;
      
            for(game=7;game<12;game++){
                //sum is equal to sum plus game.
                sum=sum+game;
            }
            //avg is equal to sum divided by team.
            avg=sum/team;
            System.out.println("Team's average is "+avg);
        }
       //print out the games and the scores that match the team name and the game number.
       System.out.println(games[game]+ " " + score[team][game]); 
    }
  }