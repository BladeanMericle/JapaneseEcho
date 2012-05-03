package org.mericle.japaneseecho;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class JapaneseEchoActivity extends Activity {

	protected static int REQUEST_CODE = 12345; // 値はてきとう

	protected EditText result = null;

	protected Button run = null;

	protected TextToSpeech tts = null;

	protected TtsOnInitListener ttsListener = null;

	/**
	 * デバッグ用ログ
	 * @param message ログメッセージ
	 */
	protected static void debug(final String message) {
		Log.d(JapaneseEchoActivity.class.getSimpleName(), message);
	}

	@Override
	protected void onStart() {
		debug("onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		debug("onRestart");
		super.onRestart();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		debug("onCreate");
		super.onCreate(savedInstanceState);

		// メイン画面のアクティビティを設定
		setContentView(R.layout.main);

		// 結果表示用のテキストボックス
		result = (EditText) findViewById(R.id.resultEditText);

		// 実行ボタン
		run = (Button) findViewById(R.id.runButton);
		run.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					// 再生中だった場合は止める
					if (ttsListener.isSuccess() && tts.isSpeaking()) {
						tts.stop();
					}

					// インテント作成 ※ここらへんは後で意味を調べよう
					Intent intent = new Intent(
							RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(
							RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					intent.putExtra(
							RecognizerIntent.EXTRA_PROMPT,
							"音声認識");
					// インテント発行
					startActivityForResult(intent, REQUEST_CODE);
				} catch (ActivityNotFoundException e) {
					// このインテントに応答できるアクティビティがインストールされていない場合
					result.setText("エラー：音声認識がインストールされていません。");
				}
			}
		});

		// 音声合成
		ttsListener = new TtsOnInitListener();
		tts = new TextToSpeech(this, ttsListener);
	}

	// アクティビティ終了時に呼び出される
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		debug("onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
		if (resultCode != RESULT_OK) { return; }

		// 自分が投げたインテントであれば応答する
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			String resultString = "";

			// 結果文字列リスト (どうやら候補がリストになっているらしい)
			ArrayList<String> resultArray =
				data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			// 第一候補のみ取得する
			if (resultArray.size() > 0) {
				resultString = resultArray.get(0);
			}

			// ボタン無効化
			run.setEnabled(false);

			// 結果を表示
			result.setText(resultString);

			// 音声合成する
			if (ttsListener.isSuccess()) {
				tts.speak(resultString, TextToSpeech.QUEUE_FLUSH, null);
			}

			// ボタン有効化
			run.setEnabled(true);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStop() {
		debug("onStop");
		super.onStop();
	}

	@Override
	protected void onResume() {
		debug("onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		debug("onPause");
		super.onPause();
	}

	@Override
	public void finish() {
		debug("finish");
		super.finish();
	}

	@Override
	protected void onDestroy() {
		debug("onDestroy");
		super.onDestroy();
	}

	protected class TtsOnInitListener implements TextToSpeech.OnInitListener {
		protected boolean result = false;

		public void onInit(int status) {
			debug("TTS - onInit(" + status + ")");
			if (TextToSpeech.SUCCESS == status) {
				if (tts.isLanguageAvailable(Locale.JAPANESE) >= TextToSpeech.LANG_AVAILABLE) {
					tts.setLanguage(Locale.JAPANESE);
					result = true;
				} else {
					debug("TTS - Japanese is not available");
				}
			}
		}

		public boolean isSuccess() {
			return result;
		}
	}
}