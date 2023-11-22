package com.onlyapp.cooltime.view.ui.dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.onlyapp.cooltime.common.dialogResize
import com.onlyapp.cooltime.common.showShortToast
import com.onlyapp.cooltime.databinding.FragmentShareDialogBinding
import java.lang.Exception

class ShareDialog : DialogFragment() {
    private var _binding : FragmentShareDialogBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mContext = checkNotNull(context) {return}
        val dialog =  checkNotNull(this.dialog){return}
        mContext.dialogResize(dialog, 0.8f, 0.5f)

        binding.btnImageShare.setOnClickListener{
            ShareTodayInfoDialog().show(childFragmentManager, null)
            activity.showShortToast("이미지 공유")
        }
        binding.btnKakaoShare.setOnClickListener{

            this.context?.let {
                // 카카오톡 설치여부 확인
                // 사용자 정의 메시지 ID

                val templateId = 100861.toLong()
                // 카카오톡 설치여부 확인
                if (ShareClient.instance.isKakaoTalkSharingAvailable(it)) {
                    // 카카오톡으로 카카오톡 공유 가능
                    ShareClient.instance.shareCustom(it, templateId) { sharingResult, error ->
                        if (error != null) {
                            Log.e(TAG, "카카오톡 공유 실패", error)
                        }
                        else if (sharingResult != null) {
                            Log.d(TAG, "카카오톡 공유 성공 ${sharingResult.intent}")
                            startActivity(sharingResult.intent)

                            // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                            Log.w(TAG, "Warning Msg: ${sharingResult.warningMsg}")
                            Log.w(TAG, "Argument Msg: ${sharingResult.argumentMsg}")
                        }
                    }
                } else {
                    // 카카오톡 미설치: 웹 공유 사용 권장
                    // 웹 공유 예시 코드
                    val sharerUrl = WebSharerClient.instance.makeCustomUrl(templateId)

                    // CustomTabs으로 웹 브라우저 열기

                    // 1. CustomTabsServiceConnection 지원 브라우저 열기
                    // ex) Chrome, 삼성 인터넷, FireFox, 웨일 등
                    try {
                        KakaoCustomTabsClient.openWithDefault(it, sharerUrl)
                    } catch(e: UnsupportedOperationException) {
                        // CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
                    }

                    // 2. CustomTabsServiceConnection 미지원 브라우저 열기
                    // ex) 다음, 네이버 등
                    try {
                        KakaoCustomTabsClient.open(it, sharerUrl)
                    } catch (e: ActivityNotFoundException) {
                        // 디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
                    }
                }
            }
            activity.showShortToast("카카오톡 공유")
        }
        binding.btnCancel.setOnClickListener{
            dismiss()
        }
    }
}