package sale.authz

default allow := false

allow if {
	input.resource == "student"
	input.action == "read"
	has_authority("STUDENT_READ")
}

allow if {
	input.resource == "student"
	input.action == "create"
	has_authority("STUDENT_CREATE")
}

has_authority(code) if {
	code in input.subject.authorities
}
