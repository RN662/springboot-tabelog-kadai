document.addEventListener('DOMContentLoaded', function() {
	const cancelButton = document.querySelector('#cancel-subscription-button');
	
	if (cancelButton) {
		cancelButton.addEventListener('click', function(event) {
			
			if (!confirm('本当に有料プランを解約しますか？')) {
				event.preventDefault();
				return;
			}
			
			cancelButton.disabled = true;
			cancelButton.textContent = '解約中...';
			
			const form = cancelButton.closest('form');
			if (form) {
				form.submit();
			}
		});
	}
});