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
import com.ncs.o2.Room.NotificationRepository.NotificationDatabase
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

        PrefManager.initialize(this)

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
    private fun postTasks(quantity: Int) {
        for (i in 1..quantity) {

            val task = Task(
                title = Faker().howIMetYourMother().quote().toString(),
                description = description4,
                id = "#T${i}${Random(System.currentTimeMillis()).nextInt(1000, 9999)}",
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
                time_STAMP = Timestamp.now(),
                assigner_email = "slow@gmail.com"
            )

            CoroutineScope(Dispatchers.Main).launch {

                repository.postTask(task) { result ->

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
                taskID = Faker().number().randomDigit().toString(),
                title = "Mention",
                message = Faker().backToTheFuture().quote().toString(),
                timeStamp = Timestamp.now().seconds,
                fromUser = Faker().funnyName().name().toString(),
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