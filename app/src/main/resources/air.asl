/* Initial beliefs */
current_pos(9,0).
home(9,0).
ground_home(10,0).
edge(8,0).
edge(11,0).
visited(9,0).
visited(10,0).
my_status(idle).
counter(0).
steps(0).

/* Initial goals */

!start.

/* Plans */

/* initialization plan */
+!start <-
    .wait(2000); /* spawn after 2s */
    ?current_pos(X,Y); hello(current_pos(X,Y)); /* pass the starting position to the environment */
    .wait(100); !explore.

/* exploration plan */
+!explore: target(found)<-
    !return_home.
+!explore: not target(found) <-
    !check_counter;
    ?current_pos(A1,B1);
    .findall(p(X,Y),air(X,Y),L1);
    .findall(p(X,Y),visited(X,Y),L2);
    if (utils.internal_actions.air.Explore(L1,L2,current_pos(A1,B1),T)){
        !execute_path([T],T); !explore;
    } else {
        !unstuck
    }.

/* when everything has been explored, start the looping strategy */
-!explore <-
    -+status(looping);
    .findall(p(X,Y),hiding(X,Y),L2);
    !loop_around(L2,3).

/* unstuck plan */
+!unstuck: target(found)<-
    !return_home.
+!unstuck: not target(found)<-
    !check_counter;
    ?current_pos(A,B);
    .findall(p(X,Y),air(X,Y)& not visited(X,Y),GOALS);
    .findall(p(F,K), edge(F,K),LIST);
    utils.internal_actions.shared.PathFinder(p(A,B),[LIST,[],[]],GOALS,G,PATH);
    !execute_path(PATH,G); !explore.

/*
When first met a ground agent, a tag(AG_NAME) is instantiated. When the same agent is returning home, tag(AG_NAME) is
cancelled. If there are no tag(_), all the ground agents are returning home, meaning that also the task is completed.
loop_around makes the drone alternate 3 POIs and home until all agents are home.
*/
+!loop_around(_,_): not tag(_)<-
    !return_home.
+!loop_around([X|T],_):T=[]<-
    /* if the list of POIs is empty, restart */
    !explore.
+!loop_around([X|T],0)<-
    /* go home and reset the counter */
    ?current_pos(A,B); ?home(A1,B1); .findall(p(F,K), edge(F,K),LIST);
    utils.internal_actions.shared.PathFinder(p(A,B),[LIST,[],[]],[p(A1,B1)],G,PATH);
    !execute_path(PATH, G);
    !loop_around(T,3).
+!loop_around([X|T],N)<-
    /* go to POI X, repeat recursively and reduce the counter */
    ?current_pos(A,B); .findall(p(F,K), edge(F,K),LIST);
    utils.internal_actions.shared.PathFinder(p(A,B),[LIST,[],[]],[X],G,PATH);
    !execute_path(PATH, G);
    !loop_around(T,N-1).

/* plan for returning home. Kill the agent when arrives. */
+!return_home <-
    -+status(returning);
    ?current_pos(A,B); ?home(A1,B1); .findall(p(F,K), edge(F,K),LIST);
    utils.internal_actions.shared.PathFinder(p(A,B),[LIST,[],[]],[p(A1,B1)],G,PATH);
    !execute_path(PATH, G);
    .print("Arrived Home");
    .wait(200); close;.my_name(MYNAME); .kill_agent(MYNAME).

/* blindly execute a path */
+!execute_path(X,_): X=[].
+!execute_path([X|_],G) <-
    !move_one_step(X);
    ?current_pos(A,B);
    .findall(p(F,K), edge(F,K),LIST);
    utils.internal_actions.shared.PathFinder(p(A,B),[LIST,[],[]],[G],G,PATH);
    !execute_path(PATH,G).

/* execute one step, updates the counter and current position. The counter is updated every 3 steps */
+!move_one_step(X)<-
    !update_pos(X);
    move(X);
    !update_steps;
    ?steps(N);
    if (N>=2){
        ?counter(C);
        .findall(processed(R,B),processed(R,B),LIST);
        utils.internal_actions.air.UpdateCounter(LIST,N2); /*compute how much to add to the counter*/
        C2 = C+N2; -+counter(C2);
        -+steps(0);
    }.

/* if the counter>=50, the drone starts looking for an agent to share information */
+!check_counter<-
    ?counter(C);
    if(C>=50){
        !choose_agent;
    }.

/*
in choosing the agent, the drone tries to don't always select the same. This is done keeping in memory a
last_talked belief.
*/
+!choose_agent<-
    .findall(N,last_seen(S,_,_,N)&not last_talked(S),LIST);
    .max(LIST,MAX);
    ?last_seen(NAME,X,Y,MAX);
    !search_agent(NAME,X,Y).

/* If choose_agent fails means that there is only 1 known agent*/
-!choose_agent<- ?last_seen(NAME,X,Y,N);!search_agent(NAME,X,Y).

/* to search the selected agent, its last position is used. Upon arrival the counter gets decremented. */
+!search_agent(NAME, X, Y)<-
    +searching(NAME);
    !find_path([p(X,Y)],G,PATH);
    -+last_talked(NAME);
    ?counter(C); C2=C-20; -+counter(C2);
    !execute_path(PATH,G).

/*finds the path towards the closest among a list of goals*/
+!find_path(GLIST, G,PATH)<-
    ?current_pos(A,B);
    .findall(p(C,D), edge(C,D),LIST);
    utils.internal_actions.shared.PathFinder(p(A,B),[LIST,[],[]],GLIST,G,PATH).

+!update_pos(X)<-
    p(X2,Y2)=X;
    -+current_pos(X2,Y2);
    utils.internal_actions.air.UpdateVisited(p(X2,Y2)).
+!update_counter(X)<-
    ?counter(N); N2 = N+X; -+counter(N2).
+!update_steps<-
    ?steps(N); N2 = N+1; -+steps(N2).

/* BB Updates*/

+street(X,Y)<- +air(X,Y).
+grass(X,Y)<-  +air(X,Y).
+fixed_obstacle(X,Y)<- +air(X,Y).
+hiding(X,Y): ground_agent(_,X,Y)<- +air(X,Y); +success(X,Y).
+hiding(X,Y)<-+air(X,Y).
+status(NAME, going(X,Y))<- +going(NAME,X,Y); .abolish(status(NAME, going(X,Y))).
+status(NAME, returning)<- .abolish(tag(NAME)).

+target(found)<- .drop_all_intentions;
    .print("TARGET FOUND!"); !return_home.


/*
Communication logic is the same by ground agents.
The two exceptions are:
 -the tag logic
 -if the air agent is searching for that agent, it drops the intention and returns exploring.
*/

+ground_agent(NAME,X,Y): status(looping)&ground_home(X,Y)<-
    .abolish(tag(NAME)).
+ground_agent(NAME,X,Y): not last_seen(NAME,_,_,_)<-
    +tag(NAME); /* to implement the logic for returning home */
    .time(H,M,S); N=H*3600+M*60+S; +last_seen(NAME,X,Y,N);
    !communicate_with_ground(NAME,X,Y).
+ground_agent(NAME,X,Y): last_seen(NAME,_,_,N1)& searching(NAME2)<-
    if(NAME = NAME2){
        -searching(NAME);
        .drop_desire(execute_path(_,_)); !explore;
    }
    .time(H,M,S); N2=H*3600+M*60+S;
    if(N1<N2){
        .abolish(last_seen(NAME,_,_,N1));
        +last_seen(NAME,X,Y,N2);
    }
    if((N2-N1)*1000>500){
        !communicate_with_ground(NAME,X,Y)
    }.
+ground_agent(NAME,X,Y): last_seen(NAME,_,_,N1)<-
    .time(H,M,S); N2=H*3600+M*60+S;
    if(N1<N2){
        .abolish(last_seen(NAME,_,_,N1));
        +last_seen(NAME,X,Y,N2);
    }
    if((N2-N1)*1000>500){
        !communicate_with_ground(NAME,X,Y)
    }.

+!communicate_with_ground(NAME,X,Y)<-
    !send_message(NAME,[fixed_obstacle,hiding,grass,success,street,visited,going]).
-!communicate_with_ground(NAME,X,Y).

/* create and send the message together with an instance of air_agent. Then update the already_sent beliefs */
+!send_message(NAME,MESSAGE)<-
    .findall(X,already_sent(NAME,X),SENT);
    utils.internal_actions.shared.CreateMessage(MESSAGE,SENT,LIST);
    .send(NAME, tell,LIST);
    !update_already_sent(NAME,LIST);
    .my_name(S); ?current_pos(A,B);
    .send(NAME, tell, air_agent(S,A,B)).

+!update_already_sent(_,L):L=[].
+!update_already_sent(NAME,[X|L])<-
    +already_sent(NAME,X);
    !update_already_sent(NAME,L).

+last_seen(NAME,X,Y,N2): .my_name(NAME)<-
    .abolish(last_seen(NAME,X,Y,N2)).
+last_seen(NAME,X,Y,N2): last_seen(NAME,_,_,N1)<-
    if(N2>N1){
        .abolish(last_seen(NAME,_,_,N1));
        +last_seen(NAME,X,Y,N2);
    }.