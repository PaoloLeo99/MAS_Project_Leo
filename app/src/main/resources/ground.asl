/* Initial beliefs */
current_pos(10,0).
edge(8,0).
edge(11,0).
street(9,1).
street(10,1).
visited(10,0).
status(idle).
home(10,0).
coordinate(true).

/* Initial goals */
!start.

/* Plans */

/* initialization plan */
+!start  <-
    .wait(300); /* wait the system starts correctly */
    ?current_pos(X,Y); hello(current_pos(X,Y)); /* pass the starting position to the environment */
    ?my_priority(N); .wait(2000*N); /* grounds leave the starting point one at time, to avoid they overlap */
    !explore.

/* exploration plan */
+!explore <-
    -+status(exploring); ?current_pos(A1,B1);
    /* retrieving the info needed for the exploration */
    .findall(p(X,Y),street(X,Y),L1); .findall(p(X,Y),visited(X,Y),L2);
    /* explore if possible, otherwise unstuck */
    if (utils.internal_actions.ground.MoveRandomly(L1,L2,current_pos(A1,B1),T)){
        !execute_and_check([T],T);
    } else {
        !unstuck
    }.

/* if everything is explored and no POIs to investigate, return home */
-!explore <-
    .findall(hiding(X,Y), hiding(X,Y)&not success(X,Y),L);
    if(L=[] & not status(returning)){
        -+status(returning);
        .drop_all_intentions;
        !return_home;
    }else{
        -+coordinate(false);
        !choose_closest_goal
    }.

/* plan to return home */
+!return_home <-
    -+status(returning); ?current_pos(A,B); ?home(A1,B1);
    /* retrieving the list with all the cells with a cost!=1 on the map and compute the path to home*/
    !create_objects_list(LIST);
    utils.internal_actions.shared.PathFinder(p(A,B),LIST,[p(A1,B1)],G,PATH);
    /* blindly execute that path */
    !execute_path(PATH, G);
    /* once arrived kill the agent */
    .print("Arrived Home"); .wait(200); close; .my_name(MYNAME); .kill_agent(MYNAME).

/*
blindly execute a path.
if the target has been found, agent can return home directly.
if the aim is to explore all POIs, while returning, grounds check also for new POIs discovery
*/
+!execute_path(P,_):P=[].
+!execute_path([X|T],G): target(found)<-
    !move_one_step(X);
    !create_objects_list(LIST); ?current_pos(A,B);
    utils.internal_actions.shared.PathFinder(p(A,B),LIST,[G],G,PATH);
    !execute_path(PATH,G).
+!execute_path([X|_],G): not target(found)<-
    !move_one_step(X);
    !create_objects_list(LIST); ?current_pos(A,B);
    .findall(p(X1,Y1), hiding(X1,Y1) & not success(X1,Y1), L);
    if(L=[]){
        utils.internal_actions.shared.PathFinder(p(A,B),LIST,[G],G,PATH);
        !execute_path(PATH,G);
    }else{
        -+coordinate(false);
        !choose_closest_goal;
    }.

/* unstuck plan */
+!unstuck<-
    ?current_pos(A,B);
    /* find all the non-visited cells */
    .findall(p(X,Y),not_visited(X,Y),GOALS);
    !create_objects_list(LIST);
    /* reach the closest */
    utils.internal_actions.shared.PathFinder(p(A,B),LIST,GOALS,G,PATH);
    !execute_and_check(PATH,G).

/*
Executes a path and rechecks at each step the closest goal using D_star_Lite.
If coordinate is true and multiple agents aim for the same goal,
the agents will assign themselves goals based on their priority score; otherwise they reach the closest goal
*/
+!execute_and_check(T,_): T=[].
+!execute_and_check([X|_],G): G=p(X1,Y1) & going(NAME,X1,Y1)&coordinate(true)<-
    ?my_priority(N); ?priority(NAME,N2);
    if(N<N2){
        !move_one_step(X);
        !choose_closest_goal;
    }else{
        !choose_closest_goal2
    }.
+!execute_and_check([X|_],G) <-
    !move_one_step(X);
    !choose_closest_goal.

/*
move_one_step executes the step action (move), updating the current position and the BB.
It is specialized depending on which cell the agent is moving to, acting accordingly.
Investigating a POI happens if there is no other ground agent already on it, otherwise hte POI is marked as success.
*/
+!move_one_step(X): target(found) & not status(returning) <-
    .drop_all_intentions; !return_home.
+!move_one_step(X): X=p(A,B) & rem_obstacle(A,B)<-
     remove_obs(X);
     move(X); !update_pos(X).
+!move_one_step(X): X=p(A,B) & hiding(A,B) & not success(A,B) & not ground_agent(_,A,B)<-
     -+status(search);
     move(X); !update_pos(X);
     search(X); +success(A,B);
     !choose_closest_goal.
+!move_one_step(X): X=p(A,B) & hiding(A,B) & ground_agent(_,A,B)<-
    +success(A,B); !choose_closest_goal.
+!move_one_step(X)<-
    move(X); !update_pos(X).

/*
Plan to find a path towards the closest non-investigated POI.
If no known POIs, explore.
*/
+!choose_closest_goal<-
    ?current_pos(A,B); .findall(p(X,Y),hiding(X,Y) & not success(X,Y),GOALS);
    !create_objects_list(LIST);
    if(utils.internal_actions.shared.PathFinder(p(A,B),LIST,GOALS,NODE,PATH)){
        p(X1,Y1)=NODE;-+status(going(X1,Y1));
        !execute_and_check(PATH, NODE);
    }else{
        !explore
    }.

/*
Plan to find a path towards the closest non-investigated POI to which no one is already going.
If no known POIs, explore.
*/
+!choose_closest_goal2<-
    ?current_pos(A,B);
    .findall(p(X,Y),hiding(X,Y) & not success(X,Y)& not going(_,X,Y),GOALS);
    !create_objects_list(LIST);
    if(utils.internal_actions.shared.PathFinder(p(A,B),LIST,GOALS,NODE,PATH)){
        p(X1,Y1)=NODE;-+status(going(X1,Y1));
        !execute_and_check(PATH, NODE);
    }else{
        !explore
    }.

/* BB update */

+street(X,Y): not visited(X,Y)<-
    +not_visited(X,Y).
+visited(X,Y): not_visited(X,Y)<-
   .abolish(not_visited(X,Y)).
+not_visited(X,Y): visited(X,Y)<-
    .abolish(not_visited(X,Y)).
+success(X,Y)<- +fixed_obstacle(X,Y); .abolish(going(_,X,Y)).
+hiding(X,Y): status(idle)<- !choose_closest_goal.
+status(NAME, going(X,Y))<- +going(NAME,X,Y); .abolish(status(NAME, going(X,Y))). /*unwrap the status belief*/
+status(NAME, exploring)<- .abolish(status(NAME, exploring)).
+going(NAME,X,Y): .my_name(NAME)<- .abolish(going(NAME,X,Y)).
+target(found): not status(returning)<- .drop_all_intentions;
    .print("TARGET FOUND!"); .broadcast(tell,target(found));
    !return_home.
+last_seen(NAME,X,Y,N2): .my_name(NAME)<-
    .abolish(last_seen(NAME,X,Y,N2)).
+last_seen(NAME,X,Y,N2): last_seen(NAME,_,_,N1)<-
    if(N2>N1){
        .abolish(last_seen(NAME,_,_,N1));
        +last_seen(NAME,X,Y,N2);
    }.

/* Communication */
/*
When a ground agent is perceived, the last_seen belief gets updated accordingly.
Then the message containing all the important beliefs gets created and send.
At first communication also the priority is shared.
If an agent is on the same position of a POI, that POI is marked as success.
The minimum interval of time between two consequent communications between two agents is 500ms.
*/
+ground_agent(NAME,X,Y): .my_name(NAME)<-
    .abolish(ground_agent(NAME,X,Y)).
+ground_agent(NAME,X,Y): not last_seen(NAME,_,_,_)<-
    .time(H,M,S); N=H*3600+M*60+S; +last_seen(NAME,X,Y,N); .my_name(MYNAME);
    ?my_priority(PRIO); .send(NAME, tell, priority(MYNAME,PRIO));
    !communicate_with_ground(NAME,X,Y).
+ground_agent(NAME,X,Y):hiding(X,Y) & last_seen(NAME,_,_,N1)<-
    .time(H,M,S); N2=H*3600+M*60+S;
    if(N1<N2){
        .abolish(last_seen(NAME,_,_,N1));
        +last_seen(NAME,X,Y,N2);
    }
    +success(X,Y);
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

/* creates and send the status and the needed beliefs */
+!communicate_with_ground(NAME,X,Y)<-
    .my_name(MYNAME); ?status(STATUS); ?current_pos(A,B);
    .send(NAME,tell,status(MYNAME,STATUS));
    !send_message(NAME,[fixed_obstacle,edge,grass,visited,hiding,success,not_visited,going,last_seen]);
    .send(NAME, tell, ground_agent(MYNAME,A,B)).
-!communicate_with_ground(NAME,X,Y).

/* creates the message filtering out the already sent beliefs */
+!send_message(NAME, FUNCTORS)<-
    .findall(X,already_sent(NAME,X),SENT);
    utils.internal_actions.shared.CreateMessage(FUNCTORS,SENT,LIST);
    .send(NAME, tell, LIST);
    !update_already_sent(NAME,LIST).

/* marks each sent belief as already_sent*/
+!update_already_sent(_,L):L=[].
+!update_already_sent(NAME,[X|L])<-
    +already_sent(NAME,X);
    !update_already_sent(NAME,L).

/* communication with air agents uses the same logic than the communication with grounds*/
+air_agent(NAME,X,Y):not last_seen(NAME,_,_,_)<-
    .time(H,M,S); N=H*3600+M*60+S; +last_seen(NAME,X,Y,N);
    !communicate_with_air(NAME,X,Y).
+air_agent(NAME,X,Y): last_seen(NAME,_,_,N1)<-
    .time(H,M,S); N2=H*3600+M*60+S;
    if(N1<N2){
        .abolish(last_seen(NAME,_,_,N1));
        +last_seen(NAME,X,Y,N2);
    }
    if((N2-N1)*1000>500){
        !communicate_with_air(NAME,X,Y)
    }.

+!communicate_with_air(NAME,X,Y)<-
    .my_name(MYNAME);
    !send_message(NAME,[fixed_obstacle,edge,grass,hiding,success,going,last_seen]);
    .send(NAME,tell, status(MYNAME,STATUS)).
-!communicate_with_air(NAME,X,Y).

/* Utility */

/*
returns a list that contains: the list of all known obstacles, the list of all known removable obstacles and
the list of all the known grass cells. This list is used as input for the PathFinder algorithm.
 */
+!create_objects_list(LIST) <-
    .findall(p(X,Y),edge(X,Y),L1);
    .findall(p(X,Y),fixed_obstacle(X,Y),L2);
     .concat(L1,L2,OBS);
    .findall(p(X,Y),rem_obstacle(X,Y),ROBS);
    .findall(p(X,Y),grass(X,Y),GRASS);
    LIST = [OBS,ROBS,GRASS].

/* called after each move. Update the current position and mark as visited the cell*/
+!update_pos(X)<-
    p(X2,Y2)=X;
    -+current_pos(X2,Y2);
    +visited(X2,Y2).