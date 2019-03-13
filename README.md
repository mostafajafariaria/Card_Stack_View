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
  # Usage
  
  code in xml :
  
  <com.fcesteghlal.cardstackview.CardStackView
  
   android:id="@+id/stackView"
            
   style="@style/cardStackStyle"
            
   android:layout_width="match_parent"
            
   android:layout_height="match_parent"
            
   />
  
  sample this project :
  
  ##<style name="cardStackStyle">
 
   name="viewsMarginTop" 48dp      
   name="viewAlpha" true      
   name="maxViews" 4     
   name="firstAlpha"1   
   name="stepAlpha" 0.07
   name="viewsMarginLeftRight" 60dp 
   name="viewAnimDuration">200 
        
   </style>
    
   sample code this activity : 
    
   List<Object> objects = new ArrayList<>()
 
   for (int i = 0; i < 6 i++)
   {
    objects.add(new Object())
   }
             
   StackViewAdapter adapter
   CardStackView cardStackView = findViewById(R.id.stackView)
   adapter = new StackViewAdapter(this, objects)
   cardStackView.setAdapter(adapter)
       
   # Image
   ![card_stack_view](https://github.com/mostafajafariaria/Card_Stack_View/blob/master/cardStack.PNG)
       
        
       
