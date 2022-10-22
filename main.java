import com.jogamp.newt.opengl.GLWindow;
import java.util.LinkedList;
GLWindow r;
 
//képek
PImage bg, template /*plane*/;
PImage cockpit;
PImage landtexture;
 
//karikák
PShape shape;
PShape plane3d;
 
//tree
PShape tree, house;
//!tree
 
//irányítás
boolean flying=true;
boolean isRight;
boolean isLeft;
boolean isBack=false;
boolean isUp;
 
boolean isRotating = false;
float rotAngle = 0;
 
//gameLogic
boolean won = false;
 
//külsö nézet
boolean thirdPerson = true;
 
//paused
boolean paused=false;
//cheat (nem kell)
boolean enabled=false;
 
//
boolean boost = false;
 
//eltelt ido
float time = 0;
 
 
static int score = 0;
 
//kamera x, y szöge, helyzete, kamera hova néz ( tx, ty, tz)
float camx, camy, camz;
float ax, ay=0;
float tx=0, ty=-1, tz=1;
 
float vel = 10;
float velUpperBound = 60;
float velLowerBound = 10;//boosthoz sebesség
 
static String[] HIGHSCORE;
PrintWriter output = null;
 
//fa class
class Tree{
  float x, y, z;
 Tree(float x, float y, float z){
  this.x=x;
  this.y=y;
  this.z=z;
 }
 void render(){
  pushMatrix();
  translate(x, y, z);
  scale(20);
 
  shape(tree);
  popMatrix();
 }
}
 
//ház
class House{
  float x, y, z;
 House(float x, float y, float z){
  this.x=x;
  this.y=y;
  this.z=z;
 }
 void render(){
  pushMatrix();
  translate(x, y, z);
  scale(0.8f);
 
  shape(house);
  popMatrix();
 }
}
 
//karika
class Target{
 
  boolean visited=false;
 float x, y, z;
 Target(float x, float y, float z){
  this.x=x;
  this.y=y;
  this.z=z;
 }
 
 void render(){
  pushMatrix();
  translate(x, y, z);
  rotateX(PI/2);
 
  shape(shape);
 
  if(visited){
    noStroke();
   fill(255, 255, 0);
   sphere(R/4);
  }
 
  //sphere(50);
  popMatrix();
 }
 
}
 
LinkedList<Tree>trees = new LinkedList<Tree>();
LinkedList<House>houses = new LinkedList<House>();
LinkedList<Target>targets = new LinkedList<Target>();
 
//karika sugara
public static int R = 200;
 
void setup(){
  loadHighScore();
  //loadtree
  tree = loadShape("Tree.obj");
  house = loadShape("house.obj");
  //!loadtree
 
  plane3d = loadShape("plane_model.obj");
  plane3d.scale(0.3);
  tree.setFill(color(0, 100, 0));
  house.setFill(color(50, 50, 0));
 
  landtexture = loadImage("landtexture.png");
  template = loadImage("map.png");
 // plane = loadImage("plane.png");
 
 // plane.resize(300, 120);
  shape=createCan(R, 40, 50);
  bg = loadImage("bg.png");
  size(700, 700, P3D);
 
  cockpit = loadImage("cockpit2.png");
  cockpit.resize(width, height);
 
  noCursor();
 
  r=(GLWindow)surface.getNative();
  r.confinePointer(true);
  setupTargets();
 
  setToDefault();
 
  output = createWriter("highscores.txt");
 
  //trees
  for(int i = 0; i < 50; i++){
  trees.add(new Tree(random(5000)-2500, 0, i*500));
  }
 
  for(int i = 0; i < 10; i++){
   houses.add(new House(random(5000)-2500, 0, i*2500));
  }
 
 
}
 
 
void setupTargets(){
  for(int i = 0; i < template.width; i++){
   for(int j = 0; j < template.height; j++){
     if(red(template.get(i, j)) == 255){
       targets.add(new Target(random(1000)-500, 600+7*(template.height-j),i*20));
     }
   }
  }
}
 
void draw(){
  background(bg);
 
 if (thirdPerson) {
    camera(
     camx -1000*tx , camy - 1000*ty, camz - 1000*tz,
     camx, camy, camz,
     0, -1, 0
    );
  }
  else {
    camera(
     camx , camy, camz,
     camx+tx, camy+ty, camz+tz,
     0, -1, 0
    );
  }
 
  //sebesség
  if (boost) {
    if (vel < velUpperBound) vel+= 0.5;
  }
  else {
    if (vel > velLowerBound) vel-= 0.5;
    else vel = velLowerBound;
  }
 
 //fények
 ambientLight(102, 102, 102);
lightSpecular(204, 204, 204);
directionalLight(102, 102, 102, 0, 4000, -500);
specular(255, 255, 255);
 
 fill(255);
 noStroke();
 pushMatrix();
 translate(500, 0, 0);
 //box(100);
 popMatrix();
 pushMatrix();
 translate(0, 500, 0);
 //box(100);
 popMatrix();
 pushMatrix();
 translate(0, 0, 500);
 //box(100);
 popMatrix();
 
 beginShape(QUADS);
 texture(landtexture);
 vertex(-50000, 0, -50000, 0, 0);
 vertex(50000, 0, -50000, 1, 0);
 vertex(50000, 0, 50000, 1, 1);
 vertex(-50000, 0, 50000, 0, 1);
 endShape();
 //!talaj
 
 //score = 10 => nyer
 if(score == 10)won = true;
 
 for(Target t:targets){
  t.render();
 }
 
 if(isRight){
  camx += sin(ax)*10;
  camz -=cos(ax)*10;
 }
 if(isLeft){
  camx -= sin(ax)*10;
  camz += cos(ax)*10;
 }
 
 if(isBack){
    camy -= ty*10;
  camx -=tx*10;
  camz -= tz*10;
 }
 
 if(isUp){
 camy+=10;
 }
 
 if (isRotating) {
   rotAngle+= PI/20;
   if (rotAngle > TWO_PI) {
     rotAngle = 0;
     isRotating = false;
   }
 }
 
 if(!paused){time+=(float)1/frameRate;}
 
 //mozgás
 if(!paused)flying=true;
 if(paused)flying = false;
 if(flying){
   camy += ty*vel;
   camx += tx*vel;
   camz += tz*vel;
 }
 //!mozgás
 
 //cam restraints
 if(camy <= 100){camy=100;
 
 }
 
 //érzékelés
 for(int i = 0; i < targets.size(); i++){
   Target t = targets.get(i);
   if(abs(t.z-camz)<30 &&  R*R > (t.x-camx)*(t.x-camx) + (t.y-camy)*(t.y-camy) && !t.visited){
     if(i >0){
       if(targets.get(i-1).visited){
       score++;
       t.visited=true;
       }
     }
     else  {
      score++;
      t.visited=true;
     }
    }
  }
 //!érzékelés
 
 for(Tree t:trees){
  t.render();
 }
 
 for(House h:houses){
  h.render();
 }
 
 if(thirdPerson){
   pushMatrix();
   translate(camx,camy,camz);
   rotateY(-HALF_PI-ax);
   rotateX(HALF_PI-ay);
   rotateZ(rotAngle);
   fill(0);
   box(40);
   shape(plane3d,0,0);
   popMatrix();
 }else {
   camera();
   hint(DISABLE_DEPTH_TEST);
   image(cockpit, 0, 0);
   hint(ENABLE_DEPTH_TEST);
 }
 camera();
 hint(DISABLE_DEPTH_TEST);
 fill(0);
 textSize(16);
 text("Score: " + score, 10,30);
 text("Time: " + time, 10, 60);
 try{
 text("Best time: " + HIGHSCORE[0].split(" ")[1],10,90);
 }catch(Exception ex){}
 text("Altitude: " + camy,10,120);
 text("Velocity: " + vel,10,150);
 
 hint(ENABLE_DEPTH_TEST);
 //ifwon0
 if(won){
  paused=true;
  fill(0, 0, 0);
  textSize(40);
  text("Pálya teljesítve " + String.format("%.1f", time) + " sec alatt!", width/8, height/4);
 }
 
 hint(ENABLE_DEPTH_TEST);
 //endofdraw
}
 
//karika generálás
PShape createCan(float r, float h, int detail) {
 
  textureMode(NORMAL);
  PShape sh = createShape();
 
  sh.beginShape(QUAD_STRIP);
 
  sh.noStroke();
  sh.fill(255, 0, 0);
  for (int i = 0; i <= detail; i++) {
    float angle = TWO_PI / detail;
    float x = sin(i * angle);
    float z = cos(i * angle);
    float u = float(i) / detail;
    sh.normal(x, 0, z);
    sh.vertex(x * r, -h/2, z * r, u, 0);
    sh.vertex(x * r, +h/2, z * r, u, 1);
  }
  sh.endShape();
  return sh;
}
 
//ujrainditás
void reset(){
 //highscore stuff
 String previous=null;
 try{
 previous = HIGHSCORE[0].split(" ")[1];
 }catch(Exception ex){}
 if(previous != null){
   if(time < Float.valueOf(previous)){
      println(Float.valueOf(previous));
      output.println("Idö: " + time);
      output.flush();
      output.close();
     
      if(score == 10){
      loadHighScore();
      }
   }
  }
  else {
    output.println("Idö: " + time);
    output.flush();
    output.close();
  }
 //!highscore stuff
 
 score=0;
 paused=false;
 flying=true;
 setToDefault();
 won=false;
 time=0;
 isRotating = false;
 rotAngle = 0;
 vel = velLowerBound;
 
 
 for(Target t:targets){
  t.visited=false;
 }
}
 
void keyPressed(){  
  if(enabled){
 if(key == 'w')flying=true;
 
  if(key == 'a'){
  isLeft=true;
 }else if(key == 'd'){
  isRight=true;
 }
 
 if(key==' ')isUp=true;
 
 if(key == 's')isBack=true;
  } else {
   if(key == ' ' && !paused){
    paused=true;
   }else if(key == ' ' && paused){
    paused=false;
   }
  }
 
  //view
  if(thirdPerson && key == 'v'){
    thirdPerson = false;
  }else if(!thirdPerson && key == 'v'){
   thirdPerson=true;
  }
 
  if(key == 'r'){
   reset();
  }
 
  if (key == 'c') {
    isRotating = true;
  }
 
}
void keyReleased(){
 if(enabled){
 
  if(key == 'w')flying=false;
  if(key == 'a'){
  isLeft=false;
 }else if(key == 'd'){
  isRight=false;
 }
 if(key==' ')isUp=false;
 
 if(key == 's')isBack=false;
 }
}
 
void mouseMoved(){
   mouseAction();
}
 
void mouseDragged() {
  mouseAction();
}
 
void mousePressed() {
  if (mouseButton == RIGHT) {
    boost = true;
  }
}
 
void mouseReleased() {
  if (mouseButton == RIGHT) {
    boost = false;
  }
}
 
void setToDefault () {
  camx = 0;
  camy = 1000;
  camz = -800;
  ax = PI/2;
  ay = PI/2;
}
 
void mouseAction() {
  if(!paused){
  ax += (width/2 - mouseX)/200.0;
  ay -= ( (height/2 - mouseY))/200.0;
  }
  if(ay <= 0)ay = 0.001f;
  if(ay >= PI)ay = PI-0.001f;
 
  ty = cos(ay);
  tx = sin(ay)*cos(ax);
  tz = sin(ay)*sin(ax);
 
   r.warpPointer(width/2, height/2);
}
 
void loadHighScore(){
 HIGHSCORE = loadStrings("highscores.txt");
}
