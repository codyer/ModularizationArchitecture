package com.spinytech.macore;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.spinytech.macore.router.LocalRouter;
import com.spinytech.macore.multiprocess.BaseApplicationLogic;
import com.spinytech.macore.multiprocess.PriorityLogicWrapper;
import com.spinytech.macore.tools.ProcessUtil;
import com.spinytech.macore.router.WideRouter;
import com.spinytech.macore.router.WideRouterApplicationLogic;
import com.spinytech.macore.router.WideRouterConnectService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by wanglei on 2016/11/25.
 */

public abstract class MaApplication extends Application {
    private ArrayList<PriorityLogicWrapper> mLogicList;
    private HashMap<String, ArrayList<PriorityLogicWrapper>> mLogicClassMap;
    public static LocalRouter localRouter;

    @CallSuper
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("application","start"+System.currentTimeMillis());
        init();
        startWideRouter();
        initializeLogic();
        dispatchLogic();
        instantiationLogic();
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onCreate();
                }
            }
        }
        Log.e("application","end"+System.currentTimeMillis());
    }

    private void init(){
        localRouter = localRouter.getInstance(this);
        mLogicClassMap = new HashMap<>();
    }
    protected void startWideRouter(){
        if(needMultipleProcess()){
            registerApplicationLogic(WideRouter.PROCESS_NAME,1000, WideRouterApplicationLogic.class);
            Intent intent = new Intent(this, WideRouterConnectService.class);
            startService(intent);
        }
    }
    public abstract void initializeAllProcessRouter();
    protected abstract void initializeLogic();
    public abstract boolean needMultipleProcess();
    protected boolean registerApplicationLogic(String processName, int priority,@NonNull Class<? extends BaseApplicationLogic> logicClass) {
        boolean result = false;
        if (null != mLogicClassMap) {
            ArrayList<PriorityLogicWrapper> tempList = mLogicClassMap.get(processName);
            if(null==tempList){
                tempList = new ArrayList<>();
                mLogicClassMap.put(processName,tempList);
            }
            if(tempList.size()>0){
                for(PriorityLogicWrapper priorityLogicWrapper : tempList){
                    if(logicClass.getName().equals(priorityLogicWrapper.logicClass.getName())){
                        throw new RuntimeException(logicClass.getName()+" has registered.");
                    }
                }
            }
            PriorityLogicWrapper priorityLogicWrapper = new PriorityLogicWrapper(priority,logicClass);
            tempList.add(priorityLogicWrapper);
        }
        return result;
    }

    private void dispatchLogic() {
        if (null != mLogicClassMap) {
            mLogicList = mLogicClassMap.get(ProcessUtil.getProcessName(this, ProcessUtil.getMyProcessId()));
        }
    }

    private void instantiationLogic() {
        if (null != mLogicList && mLogicList.size() > 0) {
            if (null != mLogicList && mLogicList.size() > 0) {
                Collections.sort(mLogicList);
                for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                    if (null != priorityLogicWrapper) {
                        try {
                            priorityLogicWrapper.instance = priorityLogicWrapper.logicClass.newInstance();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        if (null != priorityLogicWrapper.instance) {
                            priorityLogicWrapper.instance.setApplication(this);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onTerminate();
                }
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onLowMemory();
                }
            }
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onTrimMemory(level);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null != mLogicList && mLogicList.size() > 0) {
            for (PriorityLogicWrapper priorityLogicWrapper : mLogicList) {
                if (null != priorityLogicWrapper && null != priorityLogicWrapper.instance) {
                    priorityLogicWrapper.instance.onConfigurationChanged(newConfig);
                }
            }
        }
    }

}
