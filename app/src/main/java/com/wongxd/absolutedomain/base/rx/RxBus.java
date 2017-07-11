package com.wongxd.absolutedomain.base.rx;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

//参考 http://www.loongwind.com/archives/277.html
public class RxBus {

    private final Subject<Object> mBus;

    private RxBus() {
        // toSerialized method made bus thread safe
        mBus = PublishSubject.create().toSerialized();
    }


    // 单例RxBus
//    public static RxBus getDefault() {
//        return Holder.RX_BUS;
//    }

//    private static class Holder {
//        private static final RxBus RX_BUS = new RxBus();
//    }

    private static volatile RxBus defaultInstance;

    // 单例RxBus
    public static RxBus getDefault() {
        RxBus rxBus = defaultInstance;
        if (defaultInstance == null) {
            synchronized (RxBus.class) {
                rxBus = defaultInstance;
                if (defaultInstance == null) {
                    rxBus = new RxBus();
                    defaultInstance = rxBus;
                }
            }
        }
        return rxBus;
    }

    /**
     * 为了防止内存泄漏，安全退出。
     */
    public void safeExit() {
        defaultInstance = null;
    }

    /**
     * 提供了一个新的事件,单一类型
     *
     * @param obj 事件数据
     */
    public void post(Object obj) {
        mBus.onNext(obj);
    }


    /**
     * 提供了一个新的事件,根据code进行分发
     *
     * @param code 事件code
     * @param o
     */
    public void post(int code, Object o) {
        mBus.onNext(new Message(code, o));
    }

    /***
     * 获取被观察者
     *
     * @return
     */
    public Observable<Object> toObservable() {
        return mBus;
    }


    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     *
     * @param tClass 事件类型
     * @param <T>
     * @return
     */
    public <T> Observable<T> toObservable(Class<T> tClass) {
        return mBus.ofType(tClass);
    }


    /**
     * 根据传递的code和 eventType 类型返回特定类型(eventType)的 被观察者
     *
     * @param code      事件code
     * @param eventType 事件类型
     * @param <T>
     * @return
     */
    public <T> Observable<T> toObservable(final int code, final Class<T> eventType) {
        return mBus.ofType(Message.class)
                .filter(new Predicate<Message>() {
                    @Override
                    public boolean test(@NonNull Message message) throws Exception {
                        return message.getCode() == code;
                    }
                })
                .map(new Function<Message, Object>() {
                    @Override
                    public Object apply(@NonNull Message message) throws Exception {
                        return message.getObject();
                    }
                })
                .cast(eventType);
    }

    /**
     * 判断是否有订阅者
     *
     * @return
     */
    public boolean hasObservers() {
        return mBus.hasObservers();
    }


    //###########################################-----添加注解方式-----######################################################################

    private Map<Class, List<Disposable>> disposablesByEventType = new HashMap<>();//取消


    private Map<Object, List<Class>> eventTypesBySubscriber = new HashMap<>();//包含对于一个特定事件敏感的方法 的类的列表


    private Map<Class, List<SubscriberMethod>> subscriberMethodByEventType = new HashMap<>(); //一个类中的所有的subscriber方法列表


    /**
     * 注册
     *
     * @param subscriber 订阅者
     */
    public void register(Object subscriber) {
        Class<?> subClass = subscriber.getClass();
        Method[] methods = subClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                //获得参数类型
                Class[] parameterType = method.getParameterTypes();
                //参数不为空 且参数个数为1
                if (parameterType != null && parameterType.length == 1) {

                    Class eventType = parameterType[0];

                    addEventTypeToMap(subscriber, eventType);
                    Subscribe sub = method.getAnnotation(Subscribe.class);
                    int code = sub.code();
                    ThreadMode threadMode = sub.threadMode();

                    SubscriberMethod subscriberMethod = new SubscriberMethod(subscriber, method, eventType, code, threadMode);
                    addSubscriberToMap(eventType, subscriberMethod);

                    addSubscriber(subscriberMethod);
                }
            }
        }
    }


    /**
     * 将event的类型以订阅中subscriber为key保存到map里
     *
     * @param subscriber 订阅者
     * @param eventType  event类型
     */
    private void addEventTypeToMap(Object subscriber, Class eventType) {
        List<Class> eventTypes = eventTypesBySubscriber.get(subscriber);
        if (eventTypes == null) {
            eventTypes = new ArrayList<>();
            eventTypesBySubscriber.put(subscriber, eventTypes);
        }

        if (!eventTypes.contains(eventType)) {
            eventTypes.add(eventType);
        }
    }

    /**
     * 将注解方法信息以event类型为key保存到map中
     *
     * @param eventType        event类型
     * @param subscriberMethod 注解方法信息
     */
    private void addSubscriberToMap(Class eventType, SubscriberMethod subscriberMethod) {
        List<SubscriberMethod> subscriberMethods = subscriberMethodByEventType.get(eventType);
        if (subscriberMethods == null) {
            subscriberMethods = new ArrayList<>();
            subscriberMethodByEventType.put(eventType, subscriberMethods);
        }

        if (!subscriberMethods.contains(subscriberMethod)) {
            subscriberMethods.add(subscriberMethod);
        }
    }


    /**
     * 将订阅事件以event类型为key保存到map,用于取消订阅时用
     *
     * @param eventType  event类型
     * @param disposable 订阅事件
     */
    private void addSubscriptionToMap(Class eventType, Disposable disposable) {
        List<Disposable> disposables = disposablesByEventType.get(eventType);
        if (disposables == null) {
            disposables = new ArrayList<>();
            disposablesByEventType.put(eventType, disposables);
        }

        if (!disposables.contains(disposable)) {
            disposables.add(disposable);
        }
    }


    /**
     * 用RxJava添加订阅者
     *
     * @param subscriberMethod
     */
    public void addSubscriber(final SubscriberMethod subscriberMethod) {
        Observable observable;
        if (subscriberMethod.code == -1) {
            observable = toObservable(subscriberMethod.eventType);
        } else {
            observable = toObservable(subscriberMethod.code, subscriberMethod.eventType);
        }

        Disposable disposable = postToObservable(observable, subscriberMethod)
                .subscribe(new Consumer() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        callEvent(subscriberMethod.code, o);
                    }
                });
        addSubscriptionToMap(subscriberMethod.eventType, disposable);
    }


    /**
     * 用于处理订阅事件在那个线程中执行
     *
     * @param observable
     * @param subscriberMethod
     * @return
     */
    private Observable postToObservable(Observable observable, SubscriberMethod subscriberMethod) {

        switch (subscriberMethod.threadMode) {
            case CURRENT:
                break;
            case MAIN:
                observable.observeOn(AndroidSchedulers.mainThread());
                break;

            case NEW_THREAD:
                observable.observeOn(Schedulers.newThread());
                break;
            case COMPUTATION:
                observable.observeOn(Schedulers.computation());
                break;

            case IO:
                observable.observeOn(Schedulers.io());
                break;

            case TRAMPOLINE:
                observable.observeOn(Schedulers.trampoline());
                break;
            default:
                throw new IllegalStateException("Unknown thread mode: " + subscriberMethod.threadMode);
        }
        return observable;
    }


    /**
     * 回调到订阅者的方法中
     *
     * @param code   code
     * @param object obj
     */
    private void callEvent(int code, Object object) {
        Class eventClass = object.getClass();
        List<SubscriberMethod> methods = subscriberMethodByEventType.get(eventClass);
        if (methods != null && methods.size() > 0) {
            for (SubscriberMethod subscriberMethod : methods) {

                Subscribe sub = subscriberMethod.method.getAnnotation(Subscribe.class);
                int c = sub.code();
                if (c == code) {
                    subscriberMethod.invoke(object);
                }

            }
        }
    }


    /**
     * 取消注册
     *
     * @param subscriber
     */
    public void unRegister(Object subscriber) {
        List<Class> subscribedTypes = eventTypesBySubscriber.get(subscriber);
        if (subscribedTypes != null) {
            for (Class<?> eventType : subscribedTypes) {
                unSubscribeByEventType(eventType);
                unSubscribeMethodByEventType(subscriber, eventType);
            }
            eventTypesBySubscriber.remove(subscriber);
        }
    }


    /**
     * Disposable disposable.dispose()
     *
     * @param eventType
     */
    private void unSubscribeByEventType(Class eventType) {
        List<Disposable> disposables = disposablesByEventType.get(eventType);
        if (disposables != null) {
            Iterator<Disposable> iterator = disposables.iterator();
            while (iterator.hasNext()) {
                Disposable disposable = iterator.next();
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 移除subscriber对应的subscriberMethods
     *
     * @param subscriber
     * @param eventType
     */
    private void unSubscribeMethodByEventType(Object subscriber, Class eventType) {
        List<SubscriberMethod> subscriberMethods = subscriberMethodByEventType.get(eventType);
        if (subscriberMethods != null) {
            Iterator<SubscriberMethod> iterator = subscriberMethods.iterator();
            while (iterator.hasNext()) {
                SubscriberMethod subscriberMethod = iterator.next();
                if (subscriberMethod.subscriber.equals(subscriber)) {
                    iterator.remove();
                }
            }
        }
    }


}