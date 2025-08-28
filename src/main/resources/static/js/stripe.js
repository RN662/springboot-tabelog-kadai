document.addEventListener('DOMContentLoaded', function() {
	
	const stripePublicKey = window.stripePublicKey;
	const sessionId = window.sessionId;
	
	if (typeof Stripe === 'undefined') {
		console.error('Stripe.jsが読み込まれていません');
		return;
	}
	
	if (!stripePublicKey || !sessionId) {
		console.error('Stripe設定またはセッションIDが不足しています');
		return;
	}

    const stripe = Stripe(stripePublicKey);
    const paymentButton = document.querySelector('#subscription-button');

    if (paymentButton) {
	    paymentButton.addEventListener('click', function() {
		    // ボタンを無効化
		    paymentButton.disabled = true;
		    paymentButton.textContent = '処理中...';

		    // Stripe Checkoutにリダイレクト
		    stripe.redirectToCheckout({
			    sessionId: sessionId
		    }).then(function(result) {
			
			    if (result.error) {
				    console.error('Stripe error:', result.error);
				    alert('決済処理でエラーが発生しました: ' + result.error.message);

				    // ボタンを再有効化
				    paymentButton.disabled = false;
				    paymentButton.textContent = '登録';
			    }
			    
			 }).catch(function(error) {
				 console.error('Unexpected error:', error);
				 alert('予期しないエラーが発生しました');
				 
				 // ボタンを再有効化
				 paymentButton.disabled = false;
				 paymentButton.textContent = '登録';
			    
		     });
	    });
    } else {
		console.error('登録ボタンが見つかりません');
	}
});