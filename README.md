# CardStackView
CardStackView is a android library simillar stack view in android widget.You  will can perform swipe single view this widget

## How to use dependency in your project

allprojects {

 repositories {
 
  maven { 
  
  url 'https://jitpack.io'
  
  }
  
     }
     
 }
 
Add library the dependency:


dependencies {
   
  implementation 'com.github.mostafajafariaria:Card_Stack_View:V1.0'
    
  }
  ## Usage
  
  code in xml
  
  <com.fcesteghlal.cardstackview.CardStackView
            android:id="@+id/stackView"
            style="@style/cardStackStyle"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            />
  
  sample this project :
  
  <style name="cardStackStyle">
        <item name="viewsMarginTop">48dp</item>
        <item name="viewAlpha">true</item>
        <item name="maxViews">4</item>
        <item name="firstAlpha">1</item>
        <item name="stepAlpha">0.07</item>
        <item name="viewsMarginLeftRight">60dp</item>
        <item name="viewAnimDuration">200</item>
    </style>
    
   sample code this activity : 
    
    List<Object> objects = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            objects.add(new Object());
        }
        StackViewAdapter adapter;
        CardStackView cardStackView = findViewById(R.id.stackView);
        adapter = new StackViewAdapter(this, objects);
        cardStackView.setAdapter(adapter);
        
        ## Image
        
        ![image](https://github.com/mostafajafariaria/Card_Stack_View/blob/master/cardStack.PNG)
        
       
