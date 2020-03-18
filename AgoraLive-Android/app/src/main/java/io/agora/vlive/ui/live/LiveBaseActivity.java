package io.agora.vlive.ui.live;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Map;

import io.agora.rtc.Constants;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelMember;
import io.agora.vlive.R;
import io.agora.vlive.agora.RtcEventHandler;
import io.agora.vlive.agora.rtm.RtmMessageManager;
import io.agora.vlive.agora.rtm.RtmMessageListener;
import io.agora.vlive.camera.CameraProxy;
import io.agora.vlive.ui.BaseActivity;
import io.agora.vlive.utils.Global;

/**
 * Common capabilities of a live room. Such as, camera capture，
 * , agora rtc, messaging, permissions, communication with
 * back-end server, and so on.
 */
public abstract class LiveBaseActivity extends BaseActivity
        implements RtcEventHandler, RtmMessageListener {
    protected static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_REQ = 1;

    // values of a live room
    protected String roomName;
    protected String roomId;
    protected boolean isOwner;
    protected String ownerId;
    protected boolean isHost;
    protected int myRtcRole;
    protected int ownerRtcUid;
    protected int tabId;

    // Current rtc channel generated by server
    // and obtained when entering the room.
    protected String rtcChannelName;

    private RtmMessageManager mMessageManager;
    private CameraProxy mCameraProxy = CameraProxy.create(application());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRoom();
        checkPermissions();
    }

    protected void checkPermissions() {
        if (!permissionArrayGranted()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ);
        } else {
            onPermissionGranted();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean permissionArrayGranted() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ) {
            if (permissionArrayGranted()) {
                onPermissionGranted();
            } else {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    protected abstract void onPermissionGranted();

    protected CameraProxy cameraProxy() {
        return mCameraProxy;
    }

    private void initRoom() {
        Intent intent = getIntent();
        roomName = intent.getStringExtra(Global.Constants.KEY_ROOM_NAME);
        roomId = intent.getStringExtra(Global.Constants.KEY_ROOM_ID);
        isOwner = intent.getBooleanExtra(Global.Constants.KEY_IS_ROOM_OWNER, false);
        ownerId = intent.getStringExtra(Global.Constants.KEY_ROOM_OWNER_ID);
        isHost = isOwner;
        myRtcRole = isOwner ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE;
        tabId = intent.getIntExtra(Global.Constants.TAB_KEY, -1);

        mMessageManager = RtmMessageManager.instance();
        mMessageManager.init(rtmClient());
        mMessageManager.registerMessageHandler(this);
        mMessageManager.setCallbackThread(new Handler(getMainLooper()));

        proxy().registerProxyListener(this);
        registerRtcHandler(this);
    }

    protected RtmMessageManager getMessageManager() {
        return mMessageManager;
    }

    protected void joinRtcChannel() {
        rtcEngine().setClientRole(myRtcRole);
        rtcEngine().joinChannel(config().getUserProfile().getRtcToken(),
                rtcChannelName, null, (int) config().getUserProfile().getAgoraUid());
    }

    protected void joinRtmChannel() {
        mMessageManager.joinChannel(rtcChannelName, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    @Override
    public void onRtmConnectionStateChanged(int state, int reason) {

    }

    @Override
    public void onRtmTokenExpired() {

    }

    @Override
    public void onRtmPeersOnlineStatusChanged(Map<String, Integer> map) {

    }

    @Override
    public void onRtmMemberCountUpdated(int memberCount) {

    }

    @Override
    public void onRtmAttributesUpdated(List<RtmChannelAttribute> attributeList) {

    }

    @Override
    public void onRtmMemberJoined(RtmChannelMember rtmChannelMember) {

    }

    @Override
    public void onRtmMemberLeft(RtmChannelMember rtmChannelMember) {

    }

    @Override
    public void onRtmInvitedByOwner(String ownerId, String nickname) {

    }

    @Override
    public void onRtmAppliedForSeat(String ownerId, String nickname) {

    }

    @Override
    public void onRtmInvitationRejected(String peerId, String nickname) {

    }

    @Override
    public void onRtmApplicationRejected(String peerId, String nickname) {

    }

    @Override
    public void onRtmPkReceivedFromAnotherHost(String peerId, String nickname) {

    }

    @Override
    public void onRtmPkAcceptedByTargetHost(String peerId, String nickname) {

    }

    @Override
    public void onRtmPkRejectedByTargetHost(String peerId, String nickname) {

    }

    @Override
    public void onRtmChannelMessageReceived(String peerId, String nickname, String content) {

    }

    @Override
    public void onRtmHostStateChanged(String uid, int index, int operate) {

    }

    @Override
    public void onRtmPkStartStateReceived() {

    }

    @Override
    public void onRtmPkEndStateReceived() {

    }

    @Override
    public void onRtmGiftMessageReceived(String fromUid, String toUid, String giftId) {

    }

    @Override
    public void finish() {
        super.finish();
        removeRtcHandler(this);
        rtcEngine().leaveChannel();
        mMessageManager.removeMessageHandler(this);
        mMessageManager.leaveChannel(null);
    }
}
