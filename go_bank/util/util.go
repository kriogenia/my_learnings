package util

// Constants for all supported currencies
const (
	EUR = "EUR"
	USD = "USD"
	JPY = "JPY"
	GBP = "GBP"
	CAD = "CAD"
)

// IsSupportedCurrency returns true if the currency is supported, false otherwise
func IsSupportedCurrency(currency string) bool {
	switch currency {
	case EUR, USD, JPY, GBP, CAD:
		return true
	}
	return false
}
