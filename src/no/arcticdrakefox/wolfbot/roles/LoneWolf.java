package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.model.Role;


public class LoneWolf extends Wolf {

	public LoneWolf(String name) {
		super(name);
	}

	@Override
	public Role getRole() {
		return Role.lonewolf;
	}
	
	
}
