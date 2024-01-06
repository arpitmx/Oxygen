package com.ncs.o2.UI.Testing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Constants.TestingConfig
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDatabase
import com.ncs.o2.databinding.ActivityTestingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.datafaker.Faker
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class TestingActivity : AppCompatActivity() {

    @Inject
    @FirebaseRepository
    lateinit var repository: Repository

    @Inject
    lateinit var db: NotificationDatabase

    private val binding: ActivityTestingBinding by lazy {
        ActivityTestingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.goBtn.setOnClickThrottleBounceListener {
            binding.logs.text = "Logs will appear here..."
            val mode: Int = Integer.parseInt(binding.modeEt.text.toString())
            setMode(mode)
        }
        binding.logs.setOnClickThrottleBounceListener {
            val textToCopy = binding.logs.text
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Label", textToCopy)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show()
        }
       
        
        
    }

    private fun setMode(mode: Int) {
        when (mode) {
            1 -> {
                binding.testTitleTv.text = TestingConfig.TestModes.ADD_NOTIFICATIONS.toString()
                val quantity = Integer.parseInt(binding.quantityEt.text.toString())
                setUpAddNotifications(quantity)
                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
            }

            2 -> {
                binding.testTitleTv.text = TestingConfig.TestModes.ADD_TASKS.toString()
                val quantity = Integer.parseInt(binding.quantityEt.text.toString())
                postTasks(quantity)
                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
            }

            3 -> {
                binding.testTitleTv.text = TestingConfig.TestModes.ADD_NOTIFICATION_ROOM.toString()
                val quantity = Integer.parseInt(binding.quantityEt.text.toString())

                CoroutineScope(Dispatchers.Main).launch {
                    setUpAddNotificationRoomDB(quantity)
                }

                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
            }

            4 -> {
                binding.testTitleTv.text =
                    TestingConfig.TestModes.FETCH_LATEST_NOTIFICATION_FIRESTORE.toString()
                val quantity = Integer.parseInt(binding.quantityEt.text.toString())

                CoroutineScope(Dispatchers.Main).launch {
                    fetchLatestNotifications(quantity)
                }

                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
            }

            else -> {
                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
                toast("Invalid mode")
            }
        }
    }

    private fun fetchLatestNotifications(quantity: Int= 1) {

        binding.logs.text = binding.logs.text.toString().plus("\n> Fetching latest notifications from user : ${PrefManager.getCurrentUserEmail()}")

        CoroutineScope(Dispatchers.Main).launch {

            repository.getNotificationLastSeenTimeStamp { result ->
                when (result) {
                    is ServerResult.Failure -> {
                        binding.progress.gone()
                        binding.logs.text =
                            binding.logs.text.toString().plus("\n> Failed to get last time stamp : ${result.exception.message}")
                    }

                    ServerResult.Progress -> {
                        binding.progress.visible()
                        binding.logs.text =
                            binding.logs.text.toString().plus("\n> Getting last time stamp")
                    }

                    is ServerResult.Success -> {
                        val lastTimeStamp = result.data

                        binding.progress.gone()
                        binding.logs.text =
                            binding.logs.text.toString().plus("\n> Last time stamp : ${lastTimeStamp}")

                        CoroutineScope(Dispatchers.Main).launch {
                            repository.getNewNotifications(lastSeenTimeStamp = 1699385831) { result ->

                                when(result){
                                    is ServerResult.Failure -> {
                                        binding.progress.gone()
                                        binding.logs.text = binding.logs.text.toString().plus("\n> Request failed.\nException ${result.exception.message}")
                                    }
                                    ServerResult.Progress -> {
                                        binding.progress.visible()
                                        binding.logs.text = binding.logs.text.toString().plus("\n> Loading latest notifications")

                                    }
                                    is ServerResult.Success -> {

                                        binding.logs.text = binding.logs.text.toString().plus("\n\n> Matched ${result.data.size} results")
                                        binding.progress.gone()
                                        val latestNotifs = result.data
                                        for (notif in latestNotifs){
                                            binding.logs.text = binding.logs.text.toString().plus("\n> Notification ID: ${notif.notificationID} " +
                                                    "\n- Notification Time : ${notif.timeStamp} \n- Notification Mssg : ${notif.title}\n")
                                        }

                                    }
                                }

                            }
                        }


                    }
                }

            }


        }


    }

    val description : String = """
        ![logo](./art/markwon_logo.png)

        # Markwon

        [![Build](https://github.com/noties/Markwon/workflows/Build/badge.svg)](https://github.com/noties/Markwon/actions)

 
        [commonmark-spec]: https://spec.commonmark.org/0.28/
        [commonmark-java]: https://github.com/atlassian/commonmark-java/blob/master/README.md

        ## Installation

        ![stable](https://img.shields.io/maven-central/v/io.noties.markwon/core.svg?label=stable)
        ![snapshot](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.noties.markwon/core.svg?label=snapshot)

        ```kotlin
        implementation "io.noties.markwon:core:123"
        ```

        Full list of available artifacts is present in the [install section](https://noties.github.io/Markwon/docs/v4/install.html)
        of the [documentation] web-site.

        Please visit [documentation] web-site for further reference.


        > You can find previous version of Markwon in [2.x.x](https://github.com/noties/Markwon/tree/2.x.x)
        and [3.x.x](https://github.com/noties/Markwon/tree/3.x.x) branches


        ## Supported markdown features:
        * Emphasis (`*`, `_`)
        * Strong emphasis (`**`, `__`)
        * Strike-through (`~~`)
        * Headers (`#{1,6}`)
        * Links (`[]()` && `[][]`)
        * Images
        * Thematic break (`---`, `***`, `___`)
        * Quotes & nested quotes (`>{1,}`)
        * Ordered & non-ordered lists & nested ones
        * Inline code
        * Code blocks
        * Tables (*with limitations*)
        * Syntax highlight
        * LaTeX formulas
        * HTML
          * Emphasis (`<i>`, `<em>`, `<cite>`, `<dfn>`)
          * Strong emphasis (`<b>`, `<strong>`)
          * SuperScript (`<sup>`)
          * SubScript (`<sub>`)
          * Underline (`<u>`, `ins`)
          * Strike-through (`<s>`, `<strike>`, `<del>`)
          * Link (`a`)
          * Lists (`ul`, `ol`)
          * Images (`img` will require configured image loader)
          * Blockquote (`blockquote`)
          * Heading (`h1`, `h2`, `h3`, `h4`, `h5`, `h6`)
          * there is support to render any HTML tag
        * Task lists:
        - [ ] Not _done_
          - [X] **Done** with `X`
          - [x] ~~and~~ **or** small `x`
        ---

        ## Screenshots

        Taken with default configuration (except for image loading) in [sample app](./app-sample/):

        <a href="./art/mw_light_01.png"><img src="./art/mw_light_01.png" width="30%" /></a>
        <a href="./art/mw_light_02.png"><img src="./art/mw_light_02.png" width="30%" /></a>
        <a href="./art/mw_light_03.png"><img src="./art/mw_light_03.png" width="30%" /></a>
        <a href="./art/mw_dark_01.png"><img src="./art/mw_dark_01.png" width="30%" /></a>

        By default configuration uses TextView textColor for styling, so changing textColor changes style

        ---

        ## Documentation

        Please visit [documentation] web-site for reference

        [documentation]: https://noties.github.io/Markwon


        ## Consulting
        Paid consulting is available. Please reach me out at [markwon+consulting[at]noties.io](mailto:markwon+consulting@noties.io)
        to discuss your idea or a project

        ---

        # Demo
        Based on [this cheatsheet][cheatsheet]

        ---

        ## Headers
        ---
        # Header 1
        ## Header 2
        ### Header 3
        #### Header 4
  
        ---

        ## Emphasis

        Emphasis, aka italics, with *asterisks* or _underscores_.

        Strong emphasis, aka bold, with **asterisks** or __underscores__.

        Combined emphasis with **asterisks and _underscores_**.

        Strikethrough uses two tildes. ~~Scratch this.~~

        ---

        ## Lists
        1. First ordered list item
        2. Another item
          * Unordered sub-list.
        1. Actual numbers don't matter, just that it's a number
          1. Ordered sub-list
        4. And another item.

       
        * Unordered list can use asterisks
        - Or minuses
        + Or pluses

        ---

        ## Links

        [I'm an inline-style link](https://www.google.com)

        [I'm a reference-style link][Arbitrary case-insensitive reference text]

        [I'm a relative reference to a repository file](../blob/master/LICENSE)

        [You can use numbers for reference-style link definitions][1]

        Or leave it empty and use the [link text itself].

        ---

        ## Code

        Inline `code` has `back-ticks around` it.

        ```javascript
        var s = "JavaScript syntax highlighting";
        alert(s);
        ```

        ```python
        s = "Python syntax highlighting"
        print s
        ```

        ```java
        /**
         * Helper method to obtain a Parser with registered strike-through &amp; table extensions
         * &amp; task lists (added in 1.0.1)
         *
         * @return a Parser instance that is supported by this library
         * @since 1.0.0
         */
        @NonNull
        public static Parser createParser() {
          return new Parser.Builder()
              .extensions(Arrays.asList(
                  StrikethroughExtension.create(),
                  TablesExtension.create(),
                  TaskListExtension.create()
              ))
              .build();
        }
        ```

        ```xml
        <ScrollView
          android:id="@+id/scroll_view"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:layout_marginTop="?android:attr/actionBarSize">

          <TextView
            android:id="@+id/text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dip"
            android:lineSpacingExtra="2dip"
            android:textSize="16sp"
            tools:text="yo\nman" />

        </ScrollView>
        ```

 
        
    """.trimIndent()
    val description2 : String = """
        # bitscuit 🍪  [![](https://jitpack.io/v/arpitmx/bitscuit.svg)](https://jitpack.io/#arpitmx/bitscuit)

        bitscuit updater is an android library which the developers can hook into their project and use it to update their apps in just 3 lines of code. bitscuit is hosted at  <a href="https://jitpack.io/#arpitmx/bitscuit/1.0.5">Jitpack repository</a>

        ![Logo](https://github.com/arpitmx/bitscuit/assets/59350776/4b40f173-7f7c-4357-b0a0-43b7a6cb5733)



        ## Applications 
        bitscuit is suited for those application :
        - Apps which aren't hosted on any store like google play store
        - Distributing updates apps to testers
        - Apps which have more frequent updates cause app stores like playstore takes a lot of time (1-2 day) for verification of each update  
        - For updates involving minor upgrades in the app


        ## Features


        <br><img src="https://github.com/arpitmx/bitscuit/assets/59350776/77c7d735-1c1c-40e4-bb77-1f10c8a4c9c2" width="350"><br><br>


        - **Easy integration** : bitscuit can be easily integrated into any Android app with just three lines of code, handles permissions, configurations, version comparisions,etc. saving developers valuable time and effort, why recreating the wheel? right.
        <br><br>
        <p float="left">
        <img src="https://github.com/arpitmx/bitscuit/assets/59350776/3df5b3a9-3194-46f8-9013-8d1f48edf25b" width="350">
        <img src="https://github.com/arpitmx/bitscuit/assets/59350776/8c58fc11-fe35-4be0-90cd-cc7d9101ec8a)" width="350">
        </p><br><br>


        - **Seamless updates** : bitscuit ensures that app updates happen seamlessly, without interrupting user experience or requiring the user to manually update the app.

        <br><br>
        <img src="https://github.com/arpitmx/bitscuit/assets/59350776/a703cc37-19e0-4e33-9214-bb744fec87cb" width="350"><br><br>


        - **Error handling** : bitscuit handles errors and edge cases like connection problems gracefully, ensuring that the update process is as smooth and error-free as possible for both developers and users.


        ## Installation

        Installing bistcuit is very simple , you can install bitscuit using github release by downloading the latest jar file  

        ### Using Gradle 

        #### Step 1 : Use this in build.gradle(module: project)
        ```gradle
          allprojects {
        		repositories {
        			...
        			maven { url 'https://jitpack.io' }
        		}
        	}

        //For Gradle 7.0 and above add 'maven { url 'https://jitpack.io' }' in settings.gradle file


        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
                maven { url 'https://jitpack.io' }
            }
        }


        ```




        #### Step 2 : Use this in build.gradle(module: app)
        ```gradle
         dependencies {
        	    implementation 'com.github.arpitmx:bitscuit:1.0.5'
        	}
        ```

        ### Using Maven

        ```xml
        <repositories>
        	<repository>
        		<id>jitpack.io</id>
        		<url>https://jitpack.io</url>
        	</repository>
        </repositories>
        ```

        ```xml
        <dependency>
         	<groupId>com.github.arpitmx</groupId>
        	<artifactId>bitscuit</artifactId>
        	<version>1.0.6</version>
        </dependency>
        ```

        Do not forget to add internet permission in manifest if already not present
        ```xml
        <uses-permission android:name="android.permission.INTERNET" />
        ```



            
        ## Sample usage 

        ```kotlin
         ...

        //This data can be fetched from your database 
         val url = "https://example.com/update.apk"
         val latestVersion = "1.0.1"
         val changeLogs = "Change logs..."


        // Use the buitscuit builder to create the bitscuit instance 
        val bitscuit = Bitscuit.BitscuitBuilder(this)
                            .config(url = updatedURL,version="1.0.1",changeLogs="Change logs..")
                            .build() 
          
               
        // Use the listenUpdate() function to start listening for updates 
          bitscuit.listenUpdate()   

         ...                 
                            
        ```
        ## License
        ```
           Copyright (C) 2023 Alok Ranjan

           Licensed under the Apache License, Version 2.0 (the "License");
           you may not use this file except in compliance with the License.
           You may obtain a copy of the License at

               http://www.apache.org/licenses/LICENSE-2.0

           Unless required by applicable law or agreed to in writing, software
           distributed under the License is distributed on an "AS IS" BASIS,
           WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
           See the License for the specific language governing permissions and
           limitations under the License.

           
        ```

        ## Contributing to bitscuit
        All pull requests are welcome, make sure to follow the [contribution guidelines](Contribution.md)
        when you submit pull request.
        ## Links
        [![portfolio](https://img.shields.io/badge/my_portfolio-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://github.com/arpitmx/)
        [![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/alokandro/)
        [![twitter](https://img.shields.io/badge/twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/sudoarmax)


    """.trimIndent()
    val description3 : String = """
        
        This is the code which is causing problems, look at it : 
        
        ```kotlin
         private fun postTasks(quantity: Int) {
        for (i in 1..quantity) {

            val task = Task(
                title = Faker().howIMetYourMother().quote().toString(),
                description = description2,
                id = "#T$${Random(System.currentTimeMillis()).nextInt(1000, 9999)}",
                difficulty = Random(System.currentTimeMillis()).nextInt(1, 4),
                priority = Random(System.currentTimeMillis()).nextInt(1, 4),
                status = Random(System.currentTimeMillis()).nextInt(0, 3),
                assigner = Faker().funnyName().name().toString(),
                deadline = "${Random(System.currentTimeMillis()).nextInt(1, 5)} days",
                project_ID = "NCSOxygen",
                segment = "Backend", //change segments here //like Design
                section = "CI/CD\uD83C\uDF4D",  //Testing // Completed //Ready //Ongoing
                assignee_DP_URL = "https://picsum.photos/200",
                completed = false,
                duration = Random(System.currentTimeMillis()).nextInt(1, 5).toString(),
                time_STAMP = Timestamp.now()
            )

            CoroutineScope(Dispatchers.Main).launch {

                repository.postTask(task) { result ->

                    when (result) {

                        is ServerResult.Failure -> {
                            binding.progress.gone()
                            binding.logs.text = binding.logs.text.toString()
                                .plus("\n> Errors on task ")
                        }

                        ServerResult.Progress -> {
                            binding.progress.visible()
                            binding.logs.text =
                                binding.logs.text.toString().plus("\n> Running task ")
                        }

                        is ServerResult.Success -> {
                            binding.progress.gone()
                            binding.logs.text =
                                binding.logs.text.toString().plus("\n> Task is success.")
                        }

                    }
                }
            }

        }
    }
    ```
    """.trimIndent()


    val description4 = """
    ### Codeforces problem A 455 : 
        
    ```c++
            // https://codeforces.com/contest/455/problem/A
            #include <bits/stdc++.h>
            
            using namespace std;
            using ll = long long;
            
            const int N = 100000;
            ll a[N+1], dp[N+1];
            
            int main() {
              ios::sync_with_stdio(0);
              cin.tie(0);
              int n, x;
              cin >> n;
              for (int i = 0; i < n; i++) {
                cin >> x;
                a[x]++;
              }
              for (int i = 1; i <= N; i++) a[i] *= i;
              dp[0] = 0, dp[1] = a[1], dp[2] = a[2];
              for (int i = 3; i <= N; i++)
                dp[i] = a[i] + max(dp[i-2], dp[i-3]);
              ll r = max(dp[N-1], dp[N]);
              cout << r << '\n';
            }            
                                        
    ```
        
       
    
    """.trimIndent()


    val description5= """
        # Sigmo-Music 📱 



        <img width="100" alt="sbluemain1" src="https://user-images.githubusercontent.com/59350776/126187919-736034f4-90fe-41bf-b375-1fec9db017c2.png">
        Sigmo is an android app made for music aggregation and synced music on top of spotify!
          
          Status : Under Development ⚒



        #### Features 


        <div style="display:flex;">

        - See and add songs your friends are listening, to your library! 
        - Connect with friends and listen together in 1on1 sync or in rooms together with max 30 people
        - Make room, invite , listen , chat and have fun together! 
        - Monthly analysis of listened songs and features like timeline and playbacktime of songs at a given day!
        - Full fledge chatting system

        </div>

        # In-app walkthrough :

        >This is the walkthrough of Sigmo on 10th july, 2020 (OLD).


        https://user-images.githubusercontent.com/59350776/124971641-13895800-e047-11eb-81b9-56d4cf537776.mp4


        <p float="left">

          <img src="https://user-images.githubusercontent.com/59350776/129929877-8ab3bda3-7b73-45d1-8e65-e21381a5e63e.jpg" width="360" height="720">
          <img src="https://user-images.githubusercontent.com/59350776/129929901-eb1d5467-9e76-4952-be23-7074a68d2df4.jpg" width="360" height="720">
          <img src="https://user-images.githubusercontent.com/59350776/163082676-4d4f1fd2-b2e7-408d-a225-d8fed85cf43a.jpg" width="360" height="720">
          <img src="https://user-images.githubusercontent.com/59350776/163082964-116d2d3e-37bb-4538-8d7d-0c287b54962c.jpg" width="360" height="720">
          <img src="https://user-images.githubusercontent.com/59350776/163082810-35f571fb-5dc5-47a8-9fdf-b87a6322ee1a.jpg" width="360" height="720">
          
          <img src="https://user-images.githubusercontent.com/59350776/163083478-805e42a5-b865-452f-87c3-5aa53d5cfe82.jpg" width="360" height="720">
          <img src="https://user-images.githubusercontent.com/59350776/163083619-1aa7c626-0f88-4cde-b7e2-e0b09bf70cb4.jpg" width="360" height="720">
          <img src="https://user-images.githubusercontent.com/59350776/163083692-f8b3a177-5de4-4aea-9f25-ee80f21d8ee9.jpg" width="360" height="720">
          <img src="https://user-images.githubusercontent.com/59350776/163083989-fa03184f-ecfc-440d-9d3a-986508c32d54.jpg" width="360" height="720">

          
          <img src="https://user-images.githubusercontent.com/59350776/129929966-41898dfd-61ba-441a-bb54-7146b0d33253.jpg" width="360" height="720">
          <img src="https://user-images.githubusercontent.com/59350776/129929982-8297c367-a183-40bc-ad7f-b7c8889bef80.jpg" width="360" height="720">



          
        </p>

        ## Icons meaning 👉 :  
                            🔹 (Partially done)
                            ✅ (Done)
                            👷‍♀️  (Building/Working on)-(Queue number - Lesser means more priority) / (L = Learning) 
                                 (No priority number means doing along with current priority queue)
                            

        ## Future 👷‍:
        1. Have to work on 1on1 synced listening feature.
        2. Make chat system minimalistic and have more focus on the synced features.

        ### Open issues 🔴:

        Issues are open in the issues section.

        --------------------------------------------------------------------------------------------------------------------------

        <img src="https://user-images.githubusercontent.com/59350776/141678560-87a180ef-5bbc-4c9b-a89f-0108877f49cf.png" width="350">

    """.trimIndent()


    val desc6 = """
        # ElasticViews 

        <p align="center">
          <a href="https://opensource.org/licenses/MIT"><img alt="License" src="https://img.shields.io/badge/license-MIT%20License-blue.svg"/></a>
          <a href="https://android-arsenal.com/api?level=16"><img alt="API" src="https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat"/></a>
          <a href="https://travis-ci.org/skydoves/ElasticViews"><img alt="Build Status" src="https://travis-ci.org/skydoves/ElasticViews.svg?branch=master"/></a>
          <a href="https://androidweekly.net/issues/issue-336"><img alt="Android Weekly" src="https://img.shields.io/badge/Android%20Weekly-%23336-orange.svg"/></a>
          <a href="https://skydoves.github.io/libraries/elasticviews/javadoc/elasticviews/com.skydoves.elasticviews/index.html"><img alt="Javadoc" src="https://img.shields.io/badge/Javadoc-ElasticViews-yellow"/></a>
        </p>

        <p align="center">
        ✨ An easy way to implement an elastic touch effect for Android.
        </p>

        <p align="center">
        <img src="https://user-images.githubusercontent.com/24237865/72123075-73943500-33a3-11ea-883f-9009de998788.gif" width="32%"/>
        <img src="https://user-images.githubusercontent.com/24237865/72123076-73943500-33a3-11ea-92ef-0924cd0b902e.gif" width="32%"/>
        </p>

        ## Including in your project
        [![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/elasticviews.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22elasticviews%22)
        [![Kitpack](https://jitpack.io/v/skydoves/ElasticViews.svg)](https://jitpack.io/#skydoves/ElasticViews)

        #### Gradle
        Add below codes to your **root** `build.gradle` file (not your module build.gradle file).
        ```gradle
        allprojects {
            repositories {
                mavenCentral()
            }
        }
        ```
        And add a dependency code to your **module**'s `build.gradle` file.
        ```gradle
        dependencies {
            implementation "com.github.skydoves:elasticviews:2.1.0"
        }
        ```
        ## SNAPSHOT 
        [![ElasticViews](https://img.shields.io/static/v1?label=snapshot&message=elasticviews&logo=apache%20maven&color=C71A36)](https://oss.sonatype.org/content/repositories/snapshots/com/github/skydoves/elasticviews/) <br>
        Snapshots of the current development version of ElasticViews are available, which track [the latest versions](https://oss.sonatype.org/content/repositories/snapshots/com/github/skydoves/elasticviews/).
        ```Gradle
        repositories {
           maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
        }
        ```

        ## Usage
        `ElasticViews` lets we use like using normal views and gives all of the Views or GroupViews touch effect very simply.

        #### Add XML Namespace
        First add below XML Namespace inside your XML layout file.

        ```gradle
        xmlns:app="http://schemas.android.com/apk/res-auto"
        ```

        #### OnClick Method
        All of ElasticViews should be set `OnClickListener` or OnClick method. If not, nothing happens.
        ```java
        ElasticButton elasticButton = (ElasticButton)findViewById(R.id.elasticbutton);
        elasticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something
            }
        });
        ```

        ### ElasticButton
        ```gradle
        <com.skydoves.elasticviews.ElasticButton
           android:layout_width="match_parent"
           android:layout_height="wrap_content"
           android:text="Elastic Button"
           android:textColor="@android:color/white"
           android:textSize="17sp"
           app:button_cornerRadius="4dp"
           app:button_duration="250"
           app:button_scale="0.87" />
        ```

        ### ElasticCheckButton
        ```gradle
        <com.skydoves.elasticviews.ElasticCheckButton
           android:layout_width="match_parent"
           android:layout_height="45dp"
           android:background="#30354b"
           android:text="Text"
           android:textColor="@android:color/white"
           android:textStyle="bold"
           app:checkButton_cornerRadius="4dp"
           app:checkButton_alpha="0.7"
           app:checkButton_duration="400"
           app:checkButton_scale="0.9" />
        ```

        ### ElasticImageView
        ```gradle
        <com.skydoves.elasticviews.ElasticImageView
           android:layout_width="23dp"
           android:layout_height="23dp"
           android:scaleType="fitXY"
           android:src="@drawable/ic_question"
           android:tint="#3d95c9"
           app:imageView_scale="0.7"
           app:imageView_duration="300" />
        ```

        ### ElasticFloatingButton
        ```gradle
        <com.skydoves.elasticviews.ElasticFloatingActionButton
           android:layout_width="64dp"
           android:layout_height="64dp"
           android:src="@drawable/ic_add"
           android:tint="#ffffff"
           app:fabSize="normal"
           app:fabutton_duration="400"
           app:fabutton_scale="0.85" />
        ```

        ### ElasticCardView
        ```gradle
        <com.skydoves.elasticviews.ElasticCardView
          android:layout_width="match_parent"
          android:layout_height="120dp"
          app:cardCornerRadius="8dp"
          app:cardElevation="12dp"
          app:cardBackgroundColor="@color/background"
          app:cardView_duration="250"
          app:cardView_scale="0.8" >

          ...

        </com.skydoves.elasticviews.ElasticCardView>
        ```

        ### ElasticLayout
        ElasticLayout gives elastic animation to all child views.

        ```gradle
        <com.skydoves.elasticviews.ElasticLayout
          android:layout_width="match_parent"
          android:layout_height="80dp"
          app:layout_cornerRadius="4dp"
          app:layout_duration="500"
          app:layout_scale="0.85">

          <TextView
              android:id="@+id/textView0"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:text="This is"
              android:textColor="#ffffff"
              android:textSize="18sp" />

          <TextView
              android:layout_below="@+id/textView1"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:layout_alignParentBottom="true"
              android:text="ElasticLayout"
              android:textColor="#ffffff"
              android:textSize="18sp"
              android:gravity="end" />
        </com.skydoves.elasticviews.ElasticLayout>
        ```

        ### ElasticAnimation
        ElasticAnimation implements elastic animations for android views and view groups. <br>
        ```java
        new ElasticAnimation(clickedView).setScaleX(0.9f).setScaleY(0.9f).setDuration(400)
        .setOnFinishListener(onFinishListener).doAction();
        ```

        <img src="https://user-images.githubusercontent.com/24237865/72123077-742ccb80-33a3-11ea-9262-c4977983247e.gif" align="right" width="30%">

        #### ViewPropertyAnimatorListener
        we can set `ViewPropertyAnimatorListener` using `setListener` method and detect animation's status.
        ```java
        .setListener(new ViewPropertyAnimatorListener() {
           @Override
           public void onAnimationStart(View view) {
               // do something
           }

           Override
           public void onAnimationEnd(View view) {
               finishListener.onFinished();
           }

           Override
           public void onAnimationCancel(View view) {
               // do something
           }
        });
        ```

        #### Kotlin Extension
        ElasticAnimation supports kotlin extension `elasticAnimation`.
        ```kotlin
        val anim = textView.elasticAnimation(0.8f, 0.8f, 400, object: ElasticFinishListener {
            override fun onFinished() {
                // do anything
            }
        })
        anim.doAction()
        ```

        #### Kotlin dsl
        ```kotlin
        elasticAnimation(this) {
          setDuration(duration)
          setScaleX(scale)
          setScaleY(scale)
          setOnFinishListener(object : ElasticFinishListener {
               override fun onFinished() {
               onClick()
            }
          })
        }.doAction()
        ```

        #### Example : Normal Button
        we can implement animation on all of the views like below.
        ```java
        @OnClick(R.id.button)
        public void addNewAlarm(View v){
            // implements animation uising ElasticAnimation
            new ElasticAnimation(v).setScaleX(0.85f).setScaleY(0.85f).setDuration(500)
            .setOnFinishListener(new ElasticFinishListener() {
                    @Override
                    public void onFinished() {
                        // Do something after duration time
                    }
                }).doAction();
            }
        }
        ```

        #### Example : ListView Item
        So also we can implement animation on listView's items like below.
        ```java
        private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View clickedView, final int pos, long id) {
              new ElasticAnimation(clickedView).setScaleX(0.9f).setScaleY(0.9f).setDuration(400)
                .setOnFinishListener(new ElasticFinishListener() {
                      @Override
                      public void onFinished() {
                      //Do something after duration time
                      Toast.makeText(getBaseContext(), "ListViewItem" + pos, Toast.LENGTH_SHORT).show();
                      }
                  }).doAction();
                }
            };
        ```

        ## Find this library useful? :heart:
        Support it by joining __[stargazers](https://github.com/skydoves/ElasticViews/stargazers)__ for this repository. :star: <br>
        And __[follow](https://github.com/skydoves)__ me for my next creations! 🤩

        # License
        ```xml
        The MIT License (MIT)

        Copyright (c) 2017 skydoves

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in
        all copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS2 BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
        THE SOFTWARE.
        ```
    """.trimIndent()


    val desc7 = """
        # HTextView
Animation effects with custom font support to TextView

![](https://img.shields.io/hexpm/l/plug.svg)
![](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![](https://img.shields.io/badge/Android-CustomView-blue.svg)

see [iOS Effects](https://github.com/lexrus/LTMorphingLabel)    
see [Flutter Effects](https://github.com/HitenDev/flutter_effects)


---

## Screenshot


| type  | gif |
| :-- | :-- |
| Scale     | ![](https://github.com/hanks-zyh/HTextView/blob/master/screenshot/demo3.gif) |
| Evaporate | ![](https://github.com/hanks-zyh/HTextView/blob/master/screenshot/demo5.gif) |
| Fall      | ![](https://github.com/hanks-zyh/HTextView/blob/master/screenshot/demo6.gif) |
| Line      | ![](https://github.com/hanks-zyh/HTextView/blob/master/screenshot/demo7.gif) |
| Typer     |  ![](https://github.com/hanks-zyh/HTextView/blob/master/screenshot/typer.gif) |
| Rainbow   | ![](https://github.com/hanks-zyh/HTextView/blob/master/screenshot/rainbow.gif) |
| Fade      | ![](https://github.com/hanks-zyh/HTextView/blob/master/screenshot/fade.gif) |

## Usage


```
       // optional
```


### line

```
<com.hanks.htextview.line.LineTextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:gravity="right"
    android:paddingRight="10dp"
    android:text="This is LineTextView\nToday is Monday"
    android:textSize="16sp"
    app:animationDuration="3000"
    app:lineColor="#1367bc"
    app:lineWidth="4dp"/>
```

### fade

```
<com.hanks.htextview.fade.FadeTextView
    android:layout_width="240dp"
    android:layout_height="150dp"
    android:gravity="left"
    android:letterSpacing="0.08"
    android:lineSpacingMultiplier="1.3"
    android:text="This is FadeTextView"
    android:textColor="#fff"
    android:textSize="20sp"
    app:animationDuration="1500"/>
```

### typer

```
<com.hanks.htextview.typer.TyperTextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="this is init sentence."
    app:charIncrease="3"
    app:typerSpeed="80"/>
```

### rainbow

```
<com.hanks.htextview.rainbow.RainbowTextView
    android:layout_width="120dp"
    android:layout_height="wrap_content"
    android:gravity="right"
    android:text="this is init sentence"
    android:textSize="20sp"
    app:colorSpace="150dp"
    app:colorSpeed="4dp"/>
```

### scale (single line)

```
<com.hanks.htextview.scale.ScaleTextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="this is init sentence"
    android:textSize="16sp"/>
```


### evaporate (single line)

```
<com.hanks.htextview.evaporate.EvaporateTextView
    android:layout_width="match_parent"
    android:layout_height="100dp"
    android:gravity="center"
    android:paddingTop="8dp"
    android:text="this is init sentence"
    android:textSize="20sp"/>
```

### fall  (single line)

```
<com.hanks.htextview.fall.FallTextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingBottom="20dp"
    android:text="this is init sentence"
    android:textSize="16sp"/>
```

## Third Party Bindings

### React Native
You may now use this library with [React Native](https://github.com/facebook/react-native) via the module [here](https://github.com/prscX/react-native-morphing-text)


## License

This library is licensed under the [Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

See [`LICENSE`](LICENSE) for full of the license text.

    Copyright (C) 2015 [Hanks](https://github.com/hanks-zyh)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    """.trimIndent()

    private fun postTasks(quantity: Int) {
        for (i in 1..quantity) {

            val task = Task(
                title = Faker().howIMetYourMother().quote().toString(),
                description = desc7,
                id = "#T${RandomIDGenerator.generateRandomTaskId(5)}",
                difficulty = Random(System.currentTimeMillis()).nextInt(1, 4),
                priority = Random(System.currentTimeMillis()).nextInt(1, 4),
                status = Random(System.currentTimeMillis()).nextInt(1, 4),
                assigner = "m@gamil.com",
                assignee = "",
                moderators = listOf("None"),
                project_ID = "NCSOxygen",
                segment = "Backend", //change segments here //like Design
                section = "CI/CD\uD83C\uDF4D",  //Testing // Completed //Ready //Ongoing
                duration = Random(System.currentTimeMillis()).nextInt(1, 5).toString(),
                time_STAMP = Timestamp.now(),
            )

            CoroutineScope(Dispatchers.Main).launch {

                repository.postTask(task, mutableListOf()) { result ->

                    when (result) {

                        is ServerResult.Failure -> {
                            binding.progress.gone()
                            binding.logs.text = binding.logs.text.toString()
                                .plus("\n> Errors on task ${i}: ${result.exception.message}")
                        }

                        ServerResult.Progress -> {
                            binding.progress.visible()
                            binding.logs.text =
                                binding.logs.text.toString().plus("\n> Running task ${i}")
                        }

                        is ServerResult.Success -> {
                            binding.progress.gone()
                            binding.logs.text =
                                binding.logs.text.toString().plus("\n> Task ${i} is success.")
                        }

                    }
                }
            }

        }
    }

    private suspend fun setUpAddNotificationRoomDB(quantity: Int) {
        binding.logs.text = binding.logs.text.toString().plus("Testing notification room.")
        val notifDAO = db.notificationDao()

        for (task in 1..quantity) {

            val notification = Notification(
                notificationID = RandomIDGenerator.generateRandomId(),
                notificationType = NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.toString(),
                taskID = Faker().number().randomDigit().toString(),
                title = "Mention",
                message = Faker().backToTheFuture().quote().toString(),
                timeStamp = Timestamp.now().seconds,
                fromUser = Faker().funnyName().name().toString(),
                toUser = PrefManager.getCurrentUserEmail()
            )

            val job = CoroutineScope(Dispatchers.IO).launch {
                notifDAO.getNotificationById("cWq4UDL%bu^i39G")
            }

            try {
                job.join()
                binding.logs.text =
                    binding.logs.text.toString().plus("\n-> Added ${task} to Notification Database")
                binding.logs.text = binding.logs.text.toString()
                    .plus("\n-> Added at Time : ${DateTimeUtils.formatTime(notification.timeStamp)}")


            } catch (except: Exception) {
                binding.logs.text = binding.logs.text.toString()
                    .plus("\n-> Failed at ${task} due to ${except.message}")
            }
        }
    }


    private fun setUpAddNotifications(quantity: Int) {
        binding.logs.text =
            binding.logs.text.toString().plus("Sending to ${PrefManager.getCurrentUserEmail()}")

        for (task in 1..quantity) {

            val notification = Notification(
                notificationID = RandomIDGenerator.generateRandomId(),
                notificationType = NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.toString(),
                taskID = "#Testing123",
                title = "Mention",
                message = "Testing 2",
                timeStamp = 1700260821,
                fromUser = "sudoarmax@gmail.com",
                toUser = PrefManager.getCurrentUserEmail()
            )


            CoroutineScope(Dispatchers.Main).launch {

                repository.postNotification(notification) { result ->

                    when (result) {

                        is ServerResult.Failure -> {
                            binding.progress.gone()
                            binding.logs.text = binding.logs.text.toString()
                                .plus("\n> Errors on task ${task}: ${result.exception.message}")
                        }

                        ServerResult.Progress -> {
                            binding.progress.visible()
                            binding.logs.text =
                                binding.logs.text.toString().plus("\n> Running task ${task}")
                        }

                        is ServerResult.Success -> {
                            binding.progress.gone()
                            binding.logs.text =
                                binding.logs.text.toString().plus("\n> Task ${task} is success.")
                        }

                    }
                }
            }

        }

    }
}