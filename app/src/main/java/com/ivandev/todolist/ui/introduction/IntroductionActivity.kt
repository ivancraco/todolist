package com.ivandev.todolist.ui.introduction

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ivandev.todolist.R
import com.ivandev.todolist.core.adapter.state.FragmentAdapter
import com.ivandev.todolist.core.common.ISearchable
import com.ivandev.todolist.core.notification.NotificationWorker
import com.ivandev.todolist.data.dataStore
import com.ivandev.todolist.databinding.ActivityIntroductionBinding
import com.ivandev.todolist.domain.UserTask
import com.ivandev.todolist.ui.aggregation.AggregationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class IntroductionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroductionBinding
    private lateinit var splashScreen: SplashScreen
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var floatingActionButon: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var tabTexts: List<String>
    private var searchItem: MenuItem? = null
    private var currentFragPosition = 0
    private var nextFragPosition = 0
    private val introductionViewModel: IntroductionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSplashScreen()
        setBinding()
        initUi()
    }

    private fun addSplashScreen() {
        splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
    }

    private fun initUi() {
        setDateTime()
        itemBinding()
        createTabTexts()
        createChannel()
        stateAdapter()
        adapterScreenPageLimit()
        tabLayoutMediator()
        viewPagerPageListener()
        flagNotTouchable()
        initObserver()
        getDataStore()
        actionButtonClickListener()
    }

    private fun setBinding() {
        binding = ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun viewPagerPageListener() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                nextFragPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    lifecycleScope.launch {
                        if (currentFragPosition != nextFragPosition) {
                            val currentFragment =
                                supportFragmentManager.fragments[currentFragPosition]
                            if (currentFragment is ISearchable) {
                                currentFragment.completeList()
                            }
                            cancelExpandedSearchView()
                            introductionViewModel.cancelActionMode()
                            currentFragPosition = nextFragPosition
                        }
                    }
                }
            }
        })
    }

    private fun tabLayoutMediator() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTexts[position]
        }.attach()
    }

    private fun adapterScreenPageLimit() {
        viewPager.offscreenPageLimit = tabTexts.size - 1
    }

    private fun stateAdapter() {
        val fragmentStateAdapter = FragmentAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = fragmentStateAdapter
    }

    private fun actionButtonClickListener() {
        floatingActionButon.setOnClickListener {
            val intent = Intent(this@IntroductionActivity, AggregationActivity::class.java)
            activityResult.launch(intent)
            introductionViewModel.cancelActionMode()
            cancelExpandedSearchView()
        }
    }

    private fun itemBinding() {
        viewPager = binding.viewPager2
        tabLayout = binding.tabLayout
        floatingActionButon = binding.floatingActionButton
    }

    private fun setDateTime() {
        introductionViewModel.setDateTime()
    }

    private fun flagNotTouchable() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun createTabTexts() {
        tabTexts = listOf(
            getString(R.string.tab_text_1),
            getString(R.string.tab_text_2),
            getString(R.string.tab_text_3)
        )
    }

    private fun getDataStore() {
        introductionViewModel.onCreate(dataStore)
    }

    private fun initObserver() {
        initKeyObserver()
        initTreeObserver()
    }

    private fun initTreeObserver() {
        viewObserver(binding.lycMain)
    }

    private fun initKeyObserver() {
        introductionViewModel.keyReady.observe(this) { ready ->
            if (ready) {
                introductionViewModel.getAllTasks()
            }
        }
    }

    private fun selectFragmentForTask(tasks: List<UserTask>) {
        var todayList = listOf<UserTask>()
        var tomorrowList = listOf<UserTask>()
        var othersList = listOf<UserTask>()
        val date = introductionViewModel.dateTime
        val timeInMillis = date.timeInMillis

        for (task in tasks) {
            if (checkDateTime(task, date)) {
                todayList = todayList.plus(task)
            } else {
                date.add(Calendar.DAY_OF_MONTH, 1)
                if (checkDateTime(task, date)) {
                    tomorrowList = tomorrowList.plus(task)
                } else {
                    othersList = othersList.plus(task)
                }
            }
            date.timeInMillis = timeInMillis
        }
        introductionViewModel.updateTodList(todayList)
        introductionViewModel.updateTomList(tomorrowList)
        introductionViewModel.updateOthList(othersList)
    }

    private fun selectFragmentForTask(task: UserTask) {
        val date = introductionViewModel.dateTime
        val timeinMillis = date.timeInMillis

        if (checkDateTime(task, date)) {
            introductionViewModel.updateToday(task)
        } else {
            date.add(Calendar.DAY_OF_MONTH, 1)
            if (checkDateTime(task, date)) {
                introductionViewModel.updateTomorrow(task)
            } else {
                introductionViewModel.updateOther(task)
            }
        }
        date.timeInMillis = timeinMillis
    }

    private fun checkDateTime(task: UserTask, date: Calendar): Boolean {
        return (date.get(Calendar.DAY_OF_MONTH) == task.date.get(Calendar.DAY_OF_MONTH)) &&
                (date.get(Calendar.MONTH) == task.date.get(Calendar.MONTH)) &&
                (date.get(Calendar.YEAR) == task.date.get(Calendar.YEAR))
    }

    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data ?: return@registerForActivityResult
                val calendar = Calendar.getInstance()
                val date = data.getLongExtra(getString(R.string.task_date), 0)
                calendar.timeInMillis = date
                val task = UserTask(
                    id = data.getStringExtra(getString(R.string.task_key))!!,
                    name = data.getStringExtra(getString(R.string.task_name))!!,
                    date = calendar,
                    ampm = data.getStringExtra(getString(R.string.task_ampm))!!,
                    made = false,
                    notify = data.getBooleanExtra(getString(R.string.task_notify), false),
                    isSelected = false
                )
                selectFragmentForTask(task)
                createNotificationWorker(task)
                introductionViewModel.addTaskOnFireBase(task)
            }
        }

    private fun createNotificationWorker(task: UserTask) {
        if (!task.notify) return
        val currentTimeInMillis = System.currentTimeMillis()
        val timeExact = task.date.timeInMillis - currentTimeInMillis
        val testDate = Calendar.getInstance()
        testDate.timeInMillis = timeExact
        val notificationMessage = getString(R.string.notification_description_1)

        val dataTask = Data.Builder()
            .putString(getString(R.string.notification_title), task.name)
            .putString(getString(R.string.notification_message), notificationMessage)
            .putString(getString(R.string.user_key), introductionViewModel.userKey)
            .putString(getString(R.string.task_key), task.id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresStorageNotLow(false)
            .build()

        val notificationWork =
            OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInitialDelay(timeExact, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setInputData(dataTask)
                .setId(UUID.fromString(task.id))
                .build()

        WorkManager.getInstance(this).enqueue(notificationWork)
    }

    private fun viewObserver(view: View) {
        view.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (introductionViewModel.dataReady.value == true) {
                        selectFragmentForTask(introductionViewModel.toDoList.value!!)
                        binding.lycMain.viewTreeObserver?.removeOnPreDrawListener(this)
                        lightStatusBars()
                        splashScreen.setKeepOnScreenCondition { false }
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        true
                    } else false
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_introduction, menu)
        searchItem = menu?.findItem(R.id.search)
        searchView = searchItem?.actionView as SearchView

        setAddOnAttachStateChangeListener(searchView)
        setOnQueryTextListener(searchView)

        return true
    }

    private fun setOnQueryTextListener(searchView: SearchView) {
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                performSearch(newText)
                return false
            }
        })
    }

    private fun performSearch(text: String?) {
        val currentFragment = supportFragmentManager.fragments[viewPager.currentItem]
        if (currentFragment is ISearchable) {
            currentFragment.performSearch(text ?: "")
        }
    }

    private fun setAddOnAttachStateChangeListener(searchView: SearchView) {
        searchView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                val currentFragment = supportFragmentManager.fragments[viewPager.currentItem]
                if (currentFragment is ISearchable) {
                    currentFragment.clearList()
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                val currentFragment = supportFragmentManager.fragments[viewPager.currentItem]
                if (currentFragment is ISearchable) {
                    currentFragment.completeList()
                }
            }
        })
    }

    fun cancelExpandedSearchView() {
        if (searchItem != null) {
            if (searchItem!!.isActionViewExpanded) {
                searchItem!!.collapseActionView()
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.channel_id),
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun lightStatusBars() {
        WindowCompat.getInsetsController(
            window,
            View(this@IntroductionActivity)
        ).isAppearanceLightStatusBars = false
    }

}

